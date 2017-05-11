package com.gos.monitor.client.entity;

import com.gos.monitor.annotation.Mark;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xue on 2017-05-11.
 */
public class MarkMapping {
    private static final Map<String, Mark> Mapping = new ConcurrentHashMap<>();

    public static void put(String method, Mark entity) {
        Mapping.put(method, entity);
    }

    public static Mark get(String method) {
        return Mapping.get(method);
    }

    public static boolean hasKey(String method) {
        return Mapping.containsKey(method);
    }


    public static String json() {
        return Mapping.toString();
    }
}
