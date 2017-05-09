package com.gos.monitor.common.http;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by xue on 2017-04-06.
 */
public class HttpHeader {
    private Map<String, String> headers = new HashMap<>();

    public void set(String key, String value) {
        this.headers.put(key, value);
    }

    public String get(String key) {
        return this.headers.get(key);
    }

    @Override
    public String toString() {
        StringBuilder cup = new StringBuilder(512);
        for (Map.Entry<String, String> kv : headers.entrySet()) {
            cup.append(kv.getKey()).append(": ").append(kv.getValue()).append("\r\n");
        }
        return cup.toString();
    }
}
