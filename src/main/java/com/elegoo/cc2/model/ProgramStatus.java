package com.elegoo.cc2.model;

import com.google.gson.annotations.SerializedName;

/**
 * Status response from CC2 printer.
 */
public class ProgramStatus {
    @SerializedName("id")
    private int id;
    
    @SerializedName("method")
    private int method;
    
    @SerializedName("result")
    private StatusResult result;

    public int getId() {
        return id;
    }

    public int getMethod() {
        return method;
    }

    public StatusResult getResult() {
        return result;
    }

    public static class StatusResult {
        @SerializedName("error_code")
        private int errorCode;
        
        @SerializedName("machine_status")
        private MachineStatus machineStatus;
        
        @SerializedName("print_status")
        private PrintJobStatus printStatus;
        
        @SerializedName("extruder")
        private TemperatureInfo extruder;
        
        @SerializedName("heater_bed")
        private TemperatureInfo heaterBed;
        
        @SerializedName("ztemperature_sensor")
        private TemperatureInfo chamberTemperature;
        
        @SerializedName("fans")
        private FansInfo fans;
        
        @SerializedName("gcode_move_inf")
        private GcodePosition position;

        public int getErrorCode() {
            return errorCode;
        }

        public MachineStatus getMachineStatus() {
            return machineStatus;
        }

        public PrintJobStatus getPrintStatus() {
            return printStatus;
        }

        public TemperatureInfo getExtruder() {
            return extruder;
        }

        public TemperatureInfo getHeaterBed() {
            return heaterBed;
        }

        public TemperatureInfo getChamberTemperature() {
            return chamberTemperature;
        }

        public FansInfo getFans() {
            return fans;
        }

        public GcodePosition getPosition() {
            return position;
        }
    }

    public static class MachineStatus {
        @SerializedName("status")
        private int status;
        
        @SerializedName("sub_status")
        private int subStatus;
        
        @SerializedName("progress")
        private int progress;

        public int getStatus() {
            return status;
        }

        public int getSubStatus() {
            return subStatus;
        }

        public int getProgress() {
            return progress;
        }
    }

    public static class PrintJobStatus {
        @SerializedName("filename")
        private String filename;
        
        @SerializedName("uuid")
        private String uuid;
        
        @SerializedName("current_layer")
        private int currentLayer;
        
        @SerializedName("total_layer")
        private int totalLayer;
        
        @SerializedName("print_duration")
        private int printDuration;  // seconds
        
        @SerializedName("total_duration")
        private int totalDuration;  // seconds
        
        @SerializedName("remaining_time_sec")
        private int remainingTime;  // seconds
        
        @SerializedName("progress")
        private int progress;

        public String getFilename() {
            return filename;
        }

        public String getUuid() {
            return uuid;
        }

        public int getCurrentLayer() {
            return currentLayer;
        }

        public int getTotalLayer() {
            return totalLayer;
        }

        public int getPrintDuration() {
            return printDuration;
        }

        public int getTotalDuration() {
            return totalDuration;
        }

        public int getRemainingTime() {
            return remainingTime;
        }

        public int getProgress() {
            return progress;
        }
    }

    public static class TemperatureInfo {
        @SerializedName("temperature")
        private double temperature;
        
        @SerializedName("target")
        private double target;

        public double getTemperature() {
            return temperature;
        }

        public double getTarget() {
            return target;
        }
    }

    public static class FansInfo {
        @SerializedName("fan")
        private FanStatus fan;
        
        @SerializedName("aux_fan")
        private FanStatus auxFan;

        public FanStatus getFan() {
            return fan;
        }

        public FanStatus getAuxFan() {
            return auxFan;
        }

        public static class FanStatus {
            @SerializedName("speed")
            private int speed;  // 0-255
            
            @SerializedName("rpm")
            private int rpm;

            public int getSpeed() {
                return speed;
            }

            public int getRpm() {
                return rpm;
            }
        }
    }

    public static class GcodePosition {
        @SerializedName("x")
        private double x;
        
        @SerializedName("y")
        private double y;
        
        @SerializedName("z")
        private double z;
        
        @SerializedName("speed")
        private int speed;

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public int getSpeed() {
            return speed;
        }
    }
}
