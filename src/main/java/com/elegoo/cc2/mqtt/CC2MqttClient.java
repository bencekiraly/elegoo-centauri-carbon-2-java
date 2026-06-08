package com.elegoo.cc2.mqtt;

import com.elegoo.cc2.model.ProgramStatus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * MQTT client for CC2 printer communication.
 */
public class CC2MqttClient {
    private static final Logger logger = LoggerFactory.getLogger(CC2MqttClient.class);
    
    private static final String MQTT_USERNAME = "elegoo";
    private static final int MQTT_PORT = 1883;
    private static final int MQTT_KEEP_ALIVE = 60;
    private static final int HEARTBEAT_INTERVAL_MS = 10000;  // 10 seconds
    
    private final String printerIp;
    private final String serialNumber;
    private final String password;
    private final Gson gson = new Gson();
    
    private MqttClient mqttClient;
    private String clientId;
    private String requestId;
    private int nextCommandId = 1;
    private final Map<Integer, CompletableFuture<String>> pendingResponses = new ConcurrentHashMap<>();
    private final Timer heartbeatTimer = new Timer("CC2-Heartbeat", true);
    private volatile boolean connected = false;

    public CC2MqttClient(String printerIp, String serialNumber, String password) {
        this.printerIp = printerIp;
        this.serialNumber = serialNumber;
        this.password = password;
    }

    /**
     * Connect to the printer's MQTT broker.
     */
    public void connect() throws MqttException {
        generateClientAndRequestIds();
        
        String brokerUrl = "tcp://" + printerIp + ":" + MQTT_PORT;
        logger.info("Connecting to MQTT broker at {}", brokerUrl);
        
        mqttClient = new MqttClient(brokerUrl, clientId);
        
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(MQTT_USERNAME);
        options.setPassword(password.toCharArray());
        options.setKeepAliveInterval(MQTT_KEEP_ALIVE);
        options.setAutomaticReconnect(true);
        
        // Set callback for messages
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                connected = false;
                logger.warn("MQTT connection lost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                handleMessage(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Not used for this implementation
            }
        });
        
        // Connect
        mqttClient.connect(options);
        connected = true;
        logger.info("Connected to MQTT broker");
        
        // Register and subscribe
        register();
        subscribeToTopics();
        
        // Start heartbeat
        startHeartbeat();
    }

    /**
     * Disconnect from the printer.
     */
    public void disconnect() {
        stopHeartbeat();
        
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                connected = false;
                logger.info("Disconnected from MQTT broker");
            }
        } catch (MqttException e) {
            logger.error("Error during disconnect", e);
        }
    }

    /**
     * Get current printer status.
     */
    public ProgramStatus getStatus() throws Exception {
        MqttCommandMessage msg = new MqttCommandMessage(
            nextCommandId++,
            1002  // GET_STATUS method code
        );
        
        String response = sendCommand(msg);
        return gson.fromJson(response, ProgramStatus.class);
    }

    /**
     * Start a print job.
     */
    public void startPrint(String filename) throws Exception {
        JsonObject params = new JsonObject();
        params.addProperty("filename", filename);
        
        MqttCommandMessage msg = new MqttCommandMessage(
            nextCommandId++,
            1020,  // START_PRINT method code
            params
        );
        
        sendCommand(msg);
        logger.info("Started print: {}", filename);
    }

    /**
     * Pause the current print.
     */
    public void pausePrint() throws Exception {
        MqttCommandMessage msg = new MqttCommandMessage(
            nextCommandId++,
            1021  // PAUSE_PRINT method code
        );
        
        sendCommand(msg);
        logger.info("Paused print");
    }

    /**
     * Resume the paused print.
     */
    public void resumePrint() throws Exception {
        MqttCommandMessage msg = new MqttCommandMessage(
            nextCommandId++,
            1023  // RESUME_PRINT method code
        );
        
        sendCommand(msg);
        logger.info("Resumed print");
    }

    /**
     * Stop the current print.
     */
    public void stopPrint() throws Exception {
        MqttCommandMessage msg = new MqttCommandMessage(
            nextCommandId++,
            1022  // STOP_PRINT method code
        );
        
        sendCommand(msg);
        logger.info("Stopped print");
    }

    /**
     * Check if client is connected.
     */
    public boolean isConnected() {
        return connected && (mqttClient != null && mqttClient.isConnected());
    }

    // ============================================================================
    // Private methods
    // ============================================================================

    private void generateClientAndRequestIds() {
        // Generate client ID: "0cli" + 5 hex timestamp chars + random hex
        long timestamp = System.currentTimeMillis();
        String timestampHex = String.format("%x", timestamp);
        if (timestampHex.length() > 5) {
            timestampHex = timestampHex.substring(timestampHex.length() - 5);
        }
        
        Random random = new Random();
        String randomHex = String.format("%x", random.nextInt(0x1000));
        clientId = String.format("0cli%s%s", timestampHex, randomHex);
        if (clientId.length() > 10) {
            clientId = clientId.substring(0, 10);
        }
        
        // Generate request ID: 16 hex chars (UUID-like) + timestamp hex
        StringBuilder uuid = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            uuid.append(String.format("%x", random.nextInt(16)));
        }
        requestId = uuid.toString() + timestampHex;
        
        logger.debug("Generated clientId: {}", clientId);
        logger.debug("Generated requestId: {}", requestId);
    }

    private void register() throws MqttException {
        JsonObject registerMsg = new JsonObject();
        registerMsg.addProperty("client_id", clientId);
        registerMsg.addProperty("request_id", requestId);
        
        String topic = String.format("elegoo/%s/api_register", serialNumber);
        String payload = registerMsg.toString();
        
        logger.info("Sending registration request");
        mqttClient.publish(topic, payload.getBytes(StandardCharsets.UTF_8), 1, false);
    }

    private void subscribeToTopics() throws MqttException {
        // Subscribe to registration response
        String registerResponseTopic = String.format(
            "elegoo/%s/%s/register_response",
            serialNumber, requestId
        );
        mqttClient.subscribe(registerResponseTopic);
        logger.debug("Subscribed to: {}", registerResponseTopic);
        
        // Subscribe to status updates
        String statusTopic = String.format("elegoo/%s/api_status", serialNumber);
        mqttClient.subscribe(statusTopic);
        logger.debug("Subscribed to: {}", statusTopic);
        
        // Subscribe to command responses
        String responseTopic = String.format(
            "elegoo/%s/%s/api_response",
            serialNumber, clientId
        );
        mqttClient.subscribe(responseTopic);
        logger.debug("Subscribed to: {}", responseTopic);
    }

    private void startHeartbeat() {
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    JsonObject ping = new JsonObject();
                    ping.addProperty("type", "PING");
                    
                    String topic = String.format(
                        "elegoo/%s/%s/api_request",
                        serialNumber, clientId
                    );
                    
                    mqttClient.publish(
                        topic,
                        ping.toString().getBytes(StandardCharsets.UTF_8),
                        1,
                        false
                    );
                } catch (MqttException e) {
                    logger.debug("Heartbeat send failed: {}", e.getMessage());
                }
            }
        }, HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS);
        
        logger.debug("Heartbeat started");
    }

    private void stopHeartbeat() {
        heartbeatTimer.cancel();
        logger.debug("Heartbeat stopped");
    }

    private String sendCommand(MqttCommandMessage command) throws Exception {
        int cmdId = command.getId();
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingResponses.put(cmdId, future);
        
        try {
            JsonObject json = new JsonObject();
            json.addProperty("id", command.getId());
            json.addProperty("method", command.getMethod());
            json.add("params", command.getParams());
            
            String topic = String.format(
                "elegoo/%s/%s/api_request",
                serialNumber, clientId
            );
            
            String payload = json.toString();
            mqttClient.publish(
                topic,
                payload.getBytes(StandardCharsets.UTF_8),
                1,
                false
            );
            
            logger.debug("Sent command: {} (method: {})", cmdId, command.getMethod());
            
            // Wait for response
            String response = future.get(10, TimeUnit.SECONDS);
            logger.debug("Received response for command: {}", cmdId);
            return response;
        } finally {
            pendingResponses.remove(cmdId);
        }
    }

    private void handleMessage(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        logger.debug("Received message on topic {}: {}", topic, payload);
        
        try {
            JsonObject json = gson.fromJson(payload, JsonObject.class);
            
            if (json.has("id")) {
                int cmdId = json.get("id").getAsInt();
                CompletableFuture<String> future = pendingResponses.get(cmdId);
                
                if (future != null) {
                    future.complete(payload);
                }
            }
        } catch (Exception e) {
            logger.warn("Error handling message: {}", e.getMessage());
        }
    }
}
