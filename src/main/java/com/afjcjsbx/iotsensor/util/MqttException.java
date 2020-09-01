package com.afjcjsbx.iotsensor.util;

public class MqttException extends RuntimeException {
    private int errorCode;

    public MqttException(String msg) {
        super(msg);
        this.errorCode = -1;
    }

    public MqttException(int errorCode) {
        this.errorCode = errorCode;
    }

    int getErrorCode() {
        return this.errorCode;
    }
}
