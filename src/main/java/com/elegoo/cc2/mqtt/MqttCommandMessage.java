package com.elegoo.cc2.mqtt;

import com.google.gson.JsonObject;

/**
 * MQTT command message for CC2 printer.
 */
public class MqttCommandMessage {
    private int id;
    private int method;
    private JsonObject params;

    public MqttCommandMessage(int id, int method) {
        this.id = id;
        this.method = method;
        this.params = new JsonObject();
    }

    public MqttCommandMessage(int id, int method, JsonObject params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }

    public int getId() {
        return id;
    }

    public int getMethod() {
        return method;
    }

    public JsonObject getParams() {
        return params;
    }

    public void setParam(String key, Object value) {
        if (value instanceof String) {
            params.addProperty(key, (String) value);
        } else if (value instanceof Number) {
            params.addProperty(key, (Number) value);
        } else if (value instanceof Boolean) {
            params.addProperty(key, (Boolean) value);
        }
    }
}
