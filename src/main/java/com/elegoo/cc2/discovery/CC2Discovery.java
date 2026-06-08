package com.elegoo.cc2.discovery;

import com.elegoo.cc2.model.DiscoveryResponse;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Discovers CC2 printers on the local network via UDP broadcast.
 */
public class CC2Discovery {
    private static final Logger logger = LoggerFactory.getLogger(CC2Discovery.class);
    
    private static final int DISCOVERY_PORT = 52700;
    private static final int DISCOVERY_TIMEOUT_MS = 5000;
    private static final String BROADCAST_ADDR = "255.255.255.255";
    private static final String DISCOVERY_MESSAGE = "{\"id\": 0, \"method\": 7000}";
    
    private final Gson gson = new Gson();

    /**
     * Discover CC2 printers on the local network.
     * 
     * @return List of discovered printers
     */
    public List<CC2DiscoveredPrinter> discoverPrinters() {
        List<CC2DiscoveredPrinter> printers = new ArrayList<>();
        
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(DISCOVERY_TIMEOUT_MS);
            
            // Send discovery broadcast
            byte[] messageBytes = DISCOVERY_MESSAGE.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(
                messageBytes,
                messageBytes.length,
                InetAddress.getByName(BROADCAST_ADDR),
                DISCOVERY_PORT
            );
            
            logger.info("Sending discovery broadcast on port {}", DISCOVERY_PORT);
            socket.send(packet);
            
            // Receive responses
            byte[] receiveBuffer = new byte[1024];
            long endTime = System.currentTimeMillis() + DISCOVERY_TIMEOUT_MS;
            
            while (System.currentTimeMillis() < endTime) {
                try {
                    DatagramPacket responsePacket = new DatagramPacket(
                        receiveBuffer,
                        receiveBuffer.length
                    );
                    socket.receive(responsePacket);
                    
                    String response = new String(
                        responsePacket.getData(),
                        0,
                        responsePacket.getLength(),
                        StandardCharsets.UTF_8
                    );
                    
                    logger.debug("Received discovery response from {}: {}",
                        responsePacket.getAddress().getHostAddress(),
                        response
                    );
                    
                    try {
                        DiscoveryResponse discoveryResponse = gson.fromJson(
                            response,
                            DiscoveryResponse.class
                        );
                        
                        if (discoveryResponse != null && discoveryResponse.getResult() != null) {
                            CC2DiscoveredPrinter printer = new CC2DiscoveredPrinter(
                                discoveryResponse.getResult(),
                                responsePacket.getAddress().getHostAddress()
                            );
                            printers.add(printer);
                            logger.info("Discovered printer: {} ({})",
                                printer.getHostName(),
                                printer.getIpAddress()
                            );
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to parse discovery response", e);
                    }
                } catch (IOException e) {
                    // Timeout or other socket error - continue waiting
                    if (!e.getMessage().contains("Receive timed out")) {
                        logger.debug("Socket error during discovery: {}", e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Discovery failed", e);
        }
        
        logger.info("Discovery complete. Found {} printer(s)", printers.size());
        return printers;
    }
}
