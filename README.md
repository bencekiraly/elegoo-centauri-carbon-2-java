# Elegoo Centauri Carbon 2 (CC2) Java Client

A Java implementation for communicating with the Elegoo Centauri Carbon 2 (CC2) FDM printer over the local network.

## Features

✅ **Printer Discovery** - UDP broadcast discovery on port 52700
✅ **MQTT Connection** - Connect to printer's embedded MQTT broker (port 1883)
✅ **File Upload** - Upload G-code files via HTTP PUT (port 80)
✅ **Status Monitoring** - Query printer status (temperature, progress, filament, etc.)
✅ **Print Control** - Start, pause, resume, and stop print jobs
✅ **Heartbeat** - Keep connection alive with periodic heartbeat messages

## Requirements

- Java 11+
- Maven 3.6+
- CC2 printer on the same local network
- LAN-Only Mode enabled on the printer

## Building

```bash
mvn clean package
```

## Quick Start

### 1. Discover Printer

```java
CC2Discovery discovery = new CC2Discovery();
List<CC2DiscoveredPrinter> printers = discovery.discoverPrinters();

for (CC2DiscoveredPrinter printer : printers) {
    System.out.println("Found: " + printer.getHostName());
    System.out.println("IP: " + printer.getIpAddress());
    System.out.println("Serial: " + printer.getSerialNumber());
}
```

### 2. Connect to Printer

```java
ElegooCC2Client client = new ElegooCC2Client(
    "192.168.1.100",           // Printer IP
    "CC2ABCD1234567890",       // Serial number
    "123456"                    // Password (default or access code)
);

client.connect();
```

### 3. Query Printer Status

```java
ProgramStatus status = client.getStatus();

System.out.println("Machine Status: " + status.getResult().getMachineStatus().getStatus());
System.out.println("Nozzle Temp: " + status.getResult().getExtruder().getTemperature() + "°C");
System.out.println("Bed Temp: " + status.getResult().getHeaterBed().getTemperature() + "°C");
System.out.println("Print Progress: " + status.getResult().getMachineStatus().getProgress() + "%");
```

### 4. Upload and Print File

```java
String filePath = "/path/to/model.gcode";
client.uploadFile(filePath);

// Start print
client.startPrint("model.gcode");
```

### 5. Control Print

```java
// Pause print
client.pausePrint();

// Resume print
client.resumePrint();

// Stop print
client.stopPrint();

// Disconnect
client.disconnect();
```

## Protocol Documentation

CC2 uses an inverted MQTT architecture where the printer runs the MQTT broker.

### Connection Flow

1. **Discovery (UDP 52700)** - Find printer on network
2. **MQTT Connect (1883)** - Connect to printer's MQTT broker
3. **Registration** - Register client with printer
4. **Heartbeat** - Keep connection alive (every 10 seconds)
5. **Commands** - Send commands and receive responses

### Key Ports

| Port | Protocol | Purpose |
|------|----------|----------|
| 52700 | UDP | Printer discovery |
| 1883 | TCP/MQTT | Command & status (MQTT broker) |
| 80 | HTTP | File upload (PUT /upload) |
| 8080 | HTTP | Camera stream (MJPEG) |

## Authentication

The printer supports two authentication modes:

- **No Access Code** (`token_status=0`) - Use default password `123456`
- **Access Code** (`token_status=1`) - Use user-configured access code

Both modes use username: `elegoo`

## Status Codes

### Machine Status

| Code | Name | Description |
|------|------|-------------|
| 0 | INITIALIZING | Printer booting up |
| 1 | IDLE | Ready for commands |
| 2 | PRINTING | Print in progress |
| 3-4 | FILAMENT_OPERATING | Loading/unloading filament |
| 5 | AUTO_LEVELING | Bed leveling |
| 11 | FILE_TRANSFERRING | File upload/download |

## Example: Complete Print Workflow

```java
public class PrintExample {
    public static void main(String[] args) throws Exception {
        // Discover printer
        CC2Discovery discovery = new CC2Discovery();
        List<CC2DiscoveredPrinter> printers = discovery.discoverPrinters();
        
        if (printers.isEmpty()) {
            System.out.println("No printers found");
            return;
        }
        
        CC2DiscoveredPrinter discovered = printers.get(0);
        
        // Connect
        ElegooCC2Client client = new ElegooCC2Client(
            discovered.getIpAddress(),
            discovered.getSerialNumber(),
            "123456"
        );
        client.connect();
        System.out.println("Connected to " + discovered.getHostName());
        
        // Get initial status
        ProgramStatus status = client.getStatus();
        System.out.println("Printer status: " + status.getResult().getMachineStatus().getStatus());
        
        // Upload file
        String gcodeFile = "/path/to/benchy.gcode";
        System.out.println("Uploading " + gcodeFile + "...");
        client.uploadFile(gcodeFile);
        System.out.println("Upload complete");
        
        // Start print
        System.out.println("Starting print...");
        client.startPrint(new File(gcodeFile).getName());
        
        // Monitor print
        for (int i = 0; i < 60; i++) {
            status = client.getStatus();
            System.out.printf("Progress: %d%%, Nozzle: %.1f°C, Bed: %.1f°C%n",
                status.getResult().getMachineStatus().getProgress(),
                status.getResult().getExtruder().getTemperature(),
                status.getResult().getHeaterBed().getTemperature()
            );
            Thread.sleep(5000);
        }
        
        // Cleanup
        client.disconnect();
    }
}
```

## Limitations

- LAN-Only mode required (Cloud mode not supported)
- Maximum ~4 concurrent MQTT connections
- Heartbeat required every 10 seconds to maintain connection
- File upload limited to 1 MB per chunk

## License

MIT License - See LICENSE file

## References

- [Elegoo CC2 Protocol Documentation](https://github.com/danielcherubini/elegoo-homeassistant/blob/main/docs/CC2_PROTOCOL.md)
- [Official elegoo-link SDK](https://github.com/ELEGOO-3D/elegoo-link)
- [Elegoo Home Assistant Integration](https://github.com/danielcherubini/elegoo-homeassistant)
