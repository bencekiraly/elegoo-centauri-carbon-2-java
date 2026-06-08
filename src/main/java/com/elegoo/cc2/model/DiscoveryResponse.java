package com.elegoo.cc2.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response from CC2 printer discovery broadcast.
 */
public class DiscoveryResponse {
    @SerializedName("id")
    private int id;
    
    @SerializedName("result")
    private DiscoveryResult result;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DiscoveryResult getResult() {
        return result;
    }

    public void setResult(DiscoveryResult result) {
        this.result = result;
    }

    public static class DiscoveryResult {
        @SerializedName("host_name")
        private String hostName;
        
        @SerializedName("machine_model")
        private String machineModel;
        
        @SerializedName("sn")
        private String serialNumber;
        
        @SerializedName("token_status")
        private int tokenStatus;  // 0 = no access code, 1 = access code required
        
        @SerializedName("lan_status")
        private int lanStatus;    // 0 = cloud mode, 1 = LAN-only mode

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getMachineModel() {
            return machineModel;
        }

        public void setMachineModel(String machineModel) {
            this.machineModel = machineModel;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public int getTokenStatus() {
            return tokenStatus;
        }

        public void setTokenStatus(int tokenStatus) {
            this.tokenStatus = tokenStatus;
        }

        public int getLanStatus() {
            return lanStatus;
        }

        public void setLanStatus(int lanStatus) {
            this.lanStatus = lanStatus;
        }
    }
}
