package com.gos.monitor.client.entity;


import com.gooagoo.monitor.common.MonitorSettings;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xue on 2017-03-27.
 */
public class InvokeStatisticsGroup implements Serializable {

    private static TimeGroup group = new TimeGroup();

    private static final Object Lock = new Object();

    public static void statistics(InvokeTimer timer) {
        String method = timer.getMethodName();
        long nano = timer.getElapsed();
        group.statistic(method, nano, 1, timer.getHasException() ? 1 : 0);
    }

    public static TimeGroup dump() {
        TimeGroup g = group;
        synchronized (Lock) {
            group = new TimeGroup();
        }
        return g;
    }

    public static String view() {
        return group.toString();
    }

    public static class TimeGroup {

        private long startTime = System.currentTimeMillis();

        private static final int Capacity = 4096;

        Map<String, InvokeStatistics> body = new ConcurrentHashMap<>(Capacity);


        public Map<String, InvokeStatistics> get() {
            return this.body;
        }


        public void statistic(String method, long nanosec, long times) {
            InvokeStatistics is = this.body.get(method);
            if (is == null) {
                is = new InvokeStatistics();
                this.body.put(method, is);
            }
            is.incNanotime(nanosec);
            is.incInvokeTimes(times);
        }

        public void statistic(String method, long nano, long times, long exceptions) {
            InvokeStatistics is = this.body.get(method);
            if (is == null) {
                is = new InvokeStatistics();
                this.body.put(method, is);
            }
            is.incNanotime(nano);
            is.incInvokeTimes(times);
            if (exceptions > 0) {
                is.incException(exceptions);
            }
        }

        @Override
        public String toString() {
            Set<Map.Entry<String, InvokeStatistics>> set = this.body.entrySet();

            final int size = set.size();
            StringBuilder cup = new StringBuilder((128 + size - 1) + size * 156);


            cup.append("{").append("\"stime\":").append(Long.toString(startTime)).append(",")
                    .append("\"app\":\"").append(MonitorSettings.Client.AppName).append("\",")
                    .append("\"owner\":\"").append(MonitorSettings.Client.AppOwner).append("\",")
                    .append("\"contact\":\"").append(MonitorSettings.Client.AppOwnerContact).append("\",")
                    .append("\"methods\":[");
            Iterator<Map.Entry<String, InvokeStatistics>> it = set.iterator();

            final int len = size - 1;
            for (int i = 0; i < len && it.hasNext(); i++) {
                Map.Entry<String, InvokeStatistics> kv = it.next();
                InvokeStatistics is = kv.getValue();
                is.setMethod(kv.getKey());
                cup.append(is.toString()).append(",");
            }
            if (it.hasNext()) {
                Map.Entry<String, InvokeStatistics> kv = it.next();
                InvokeStatistics is = kv.getValue();
                is.setMethod(kv.getKey());
                cup.append(is.toString());
            }
            cup.append("]}");
            return cup.toString();
        }
    }
}
