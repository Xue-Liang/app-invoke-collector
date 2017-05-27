package com.gos.monitor.server.manager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xue on 2017-05-24.
 */
public class NormalResponse implements Serializable {
    public enum Status implements Serializable {
        OK, Error
    }

    private int errCode = -1;
    private Status status = Status.OK;
    private String message;
    private Map<String, Object> body = new HashMap<>();

    private NormalResponse() {
    }

    public static NormalResponse create() {
        return new NormalResponse();
    }

    public NormalResponse ok() {
        this.errCode = 1;
        this.setStatus(Status.OK);
        return this;
    }

    public NormalResponse message(String message) {
        this.message = message;
        return this;
    }

    public NormalResponse err(int errorCode) {
        this.setStatus(Status.Error);
        this.errCode = errorCode;
        return this;
    }

    public String getStatus() {
        return status.name();
    }

    private void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public NormalResponse put(String key, Object value) {
        this.body.put(key, value);
        return this;
    }

    public NormalResponse remove(String key) {
        this.body.remove(key);
        return this;
    }
}
