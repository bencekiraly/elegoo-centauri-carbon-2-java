package com.elegoo.cc2.discovery;

import com.elegoo.cc2.model.DiscoveryResponse;

/**
 * Represents a CC2 printer discovered via UDP broadcast.
 */
public class CC2DiscoveredPrinter {
    private final String hostName;
    private final String machineModel;
    private final String serialNumber;
    private final String ipAddress;
    private final int tokenStatus;  // 0 = no access code, 1 = access code required
    private final int lanStatus;    // 0 = cloud mode, 1 = LAN-only mode

    public CC2DiscoveredPrinter(DiscoveryResponse.DiscoveryResult result, String ipAddress) {
        this.hostName = result.getHostName();
        this.machineModel = result.getMachineModel();
        this.serialNumber = result.getSerialNumber();
        this.ipAddress = ipAddress;
        this.tokenStatus = result.getTokenStatus();
        this.lanStatus = result.getLanStatus();
    }

    public String getHostName() {
        return hostName;
    }

    public String getMachineModel() {
        return machineModel;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getTokenStatus() {
        return tokenStatus;
    }

    public int getLanStatus() {
        return lanStatus;
    }

    /**
     * Check if printer requires access code for authentication.
     */
    public boolean requiresAccessCode() {
        return tokenStatus == 1;
    }

    /**
     * Check if printer is in LAN-only mode.
     */
    public boolean isLanMode() {
        return lanStatus == 1;
    }

    @Override
    public String toString() {
        return String.format(
            "CC2Printer{name='%s', model='%s', sn='%s', ip='%s', lanMode=%s, needsAccessCode=%s}",
            hostName, machineModel, serialNumber, ipAddress, isLanMode(), requiresAccessCode()
        );
    }
}
