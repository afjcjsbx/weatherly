package com.afjcjsbx.iotsensor.util;

public class NodeException extends RuntimeException {
    private int errorCode;

    public NodeException(String msg) {
        super(msg);
        this.errorCode = -1;
    }

    public NodeException(int errorCode) {
        this.errorCode = errorCode;
    }

    int getErrorCode() {
        return this.errorCode;
    }
}
