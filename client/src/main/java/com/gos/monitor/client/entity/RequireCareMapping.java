package com.gos.monitor.client.entity;

import com.gos.monitor.annotation.RequireCare;
import com.gos.monitor.common.io.SIO;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xue on 2017-05-11.
 */
public class RequireCareMapping {

    private static final Map<String, RequireCare> Mapping = new ConcurrentHashMap<>();

    private RequireCareMapping() {

    }

    public static void put(String method, RequireCare entity) {
        if (method == null || entity == null) {
            return;
        }
        Mapping.put(method, entity);
    }

    public static RequireCare get(String method) {
        return Mapping.get(method);
    }

    public static boolean hasKey(String method) {
        return Mapping.containsKey(method);
    }

    public static String json() {
        StringBuilder cup = new StringBuilder(256);
        cup.append("{");
        Set<Map.Entry<String, RequireCare>> entries = Mapping.entrySet();
        Iterator<Map.Entry<String, RequireCare>> it = entries.iterator();
        for (int i = 0, len = entries.size() - 1; i < len && it.hasNext();
             i++) {
            Map.Entry<String, RequireCare> kv = it.next();
            String key = kv.getKey();
            RequireCare value = kv.getValue();
            cup.append("\"").append(key).append("\":{\"name\":\"")
                    .append(value.name()).append("\",\"level\":").append(value.level()).append(",\"description\":\"")
                    .append(value.description()).append("\",\"maxAverageTime\":").append(value.maxAverageTime()).append(",\"maxError\":")
                    .append(value.maxError()).append("},");
        }
        if (it.hasNext()) {
            Map.Entry<String, RequireCare> kv = it.next();
            String key = kv.getKey();
            RequireCare value = kv.getValue();
            cup.append("\"").append(key).append("\":{\"name\":\"")
                    .append(value.name()).append("\",\"level\":").append(value.level()).append(",\"description\":\"")
                    .append(value.description()).append("\",\"maxAverageTime\":").append(value.maxAverageTime()).append(",\"maxError\":")
                    .append(value.maxError()).append("},");
        }
        return Mapping.toString();
    }
}
