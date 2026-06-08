package com.elegoo.cc2.client;

import com.elegoo.cc2.model.ProgramStatus;
import com.elegoo.cc2.mqtt.CC2MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Main client for Elegoo CC2 printer communication.
 * 
 * Provides high-level API for:
 * - Discovering printers
 * - Connecting and authentication
 * - Querying status
 * - Uploading files
 * - Controlling print jobs
 */
public class ElegooCC2Client {
    private static final Logger logger = LoggerFactory.getLogger(ElegooCC2Client.class);
    
    private final String printerIp;
    private final String serialNumber;
    private final String password;
    private final CC2MqttClient mqttClient;

    /**
     * Create a new CC2 client.
     * 
     * @param printerIp Printer IP address
     * @param serialNumber Printer serial number
     * @param password MQTT password (default: "123456" or access code)
     */
    public ElegooCC2Client(String printerIp, String serialNumber, String password) {
        this.printerIp = printerIp;
        this.serialNumber = serialNumber;
        this.password = password;
        this.mqttClient = new CC2MqttClient(printerIp, serialNumber, password);
    }

    /**
     * Connect to the printer.
     */
    public void connect() throws Exception {
        logger.info("Connecting to printer at {}", printerIp);
        mqttClient.connect();
        logger.info("Successfully connected to printer {}", serialNumber);
    }

    /**
     * Disconnect from the printer.
     */
    public void disconnect() {
        logger.info("Disconnecting from printer");
        mqttClient.disconnect();
    }

    /**
     * Get the current printer status.
     */
    public ProgramStatus getStatus() throws Exception {
        return mqttClient.getStatus();
    }

    /**
     * Start a print job.
     */
    public void startPrint(String filename) throws Exception {
        mqttClient.startPrint(filename);
    }

    /**
     * Pause the current print.
     */
    public void pausePrint() throws Exception {
        mqttClient.pausePrint();
    }

    /**
     * Resume a paused print.
     */
    public void resumePrint() throws Exception {
        mqttClient.resumePrint();
    }

    /**
     * Stop the current print.
     */
    public void stopPrint() throws Exception {
        mqttClient.stopPrint();
    }

    /**
     * Upload a G-code file to the printer.
     * 
     * @param filePath Path to the G-code file
     */
    public void uploadFile(String filePath) throws Exception {
        logger.info("Uploading file: {}", filePath);
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }
        
        // TODO: Implement HTTP file upload
        // For now, this is a placeholder
        logger.info("File upload functionality to be implemented");
    }

    /**
     * Check if the client is connected.
     */
    public boolean isConnected() {
        return mqttClient.isConnected();
    }
}
