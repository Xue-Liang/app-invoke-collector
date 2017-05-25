package com.gos.monitor.client.mapping;

import com.gos.monitor.annotation.RequireCare;
import com.gos.monitor.common.io.SIO;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 方法名与注解的映射
 * Created by xue on 2017-05-11.
 */
public class RequireCareMapping {

    private static final Map<String, RequireCare> MehtodMapping = new ConcurrentHashMap<>(1024);

    private static final Set<String> ReflectedClassSet = new HashSet<>(128);

    private RequireCareMapping() {

    }

    public synchronized static boolean setClass(String klass) {
        return hasClass(klass) ? true : ReflectedClassSet.add(klass);
    }

    public static void put(String method, RequireCare entity) {
        if (method == null || entity == null) {
            return;
        }
        MehtodMapping.put(method, entity);
    }


    public static RequireCare get(String method) {
        return MehtodMapping.get(method);
    }

    public static String getRequireCareAsString(String method) {
        RequireCare mark = get(method);
        StringBuilder cup = new StringBuilder(128);
        if (null == mark) {
            cup.append("{}");
        } else {
            cup.append("{")
                    .append("\"name\":\"").append(mark.name()).append("\",")
                    .append("\"level\":\"").append(mark.level().name()).append("\",")
                    .append("\"depict\":\"").append(mark.description()).append("\",")
                    .append("\"maxAverageTime\":").append(Integer.toString(mark.maxAverageTime())).append(",")
                    .append("\"minTPS\":").append(Integer.toString(mark.minTPS())).append(",")
                    .append("\"maxError\":").append(Integer.toString(mark.maxError())).append("}");
        }
        return cup.toString();
    }


    public static boolean hasKey(String method) {
        return MehtodMapping.containsKey(method);
    }

    public static boolean notHasKey(String method) {
        return !hasKey(method);
    }

    public static boolean hasClass(String klass) {
        return ReflectedClassSet.contains(klass);
    }

    public static String json() {
        StringBuilder cup = new StringBuilder(256);
        cup.append("{");
        Set<Map.Entry<String, RequireCare>> entries = MehtodMapping.entrySet();
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
        return MehtodMapping.toString();
    }

}
