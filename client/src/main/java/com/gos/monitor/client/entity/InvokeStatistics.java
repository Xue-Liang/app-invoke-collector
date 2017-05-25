package com.gos.monitor.client.entity;

import com.gos.monitor.client.mapping.RequireCareMapping;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by xue on 17-1-23.
 */
public class InvokeStatistics {
    /**
     * 被调用的方法全名
     * method name
     */
    private String method;

    /**
     * 方法被调用的次数
     * invoke counter
     */
    private AtomicLong times = new AtomicLong(0);

    /**
     * 调用总耗时.单位是纳秒
     * elapsed nanoseconds
     */
    private AtomicLong nanotime = new AtomicLong(0);


    private AtomicLong exceptions = new AtomicLong(0);

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public AtomicLong getTimes() {
        return times;
    }

    public void setTimes(AtomicLong invoked) {
        this.times = invoked;
    }

    public AtomicLong getNanotime() {
        return nanotime;
    }

    public void setNanotime(AtomicLong nanosec) {
        this.nanotime = nanosec;
    }

    public void incInvokeTimes(long times) {
        this.times.getAndAdd(times);
    }

    public void incNanotime(long nano) {
        this.nanotime.getAndAdd(nano);
    }

    public void incException(long exceptions) {
        this.exceptions.getAndAdd(exceptions);
    }

    @Override
    public String toString() {
        long invoked = times.longValue();
        if (invoked < 1) {
            return "{}";
        }
        long million = nanotime.longValue() / 1_000_000;
        long errors = exceptions.longValue();

        //估算出平均每次调用耗时(毫秒)
        long everytime = invoked > 0 ? (million / invoked) : 0;

        //估算出平均每秒能执行多少次
        long tps = (invoked * 1000) / (million < 1 ? 1 : million);

        StringBuilder cup = new StringBuilder(1024 << 2);
        cup.append("{");
        cup.append("\"method\":\"").append(method).append("\",")
                .append("\"totalTimes\":").append(Long.toString(invoked)).append(",")
                .append("\"totalNanoTime\":").append(Long.toString(nanotime.longValue())).append(",")
                .append("\"errors\":").append(Long.toString(errors)).append(",")
                .append("\"everyTime\":").append(Long.toString(everytime < 1 ? 1 : everytime)).append(",")
                .append("\"tps\":").append(Long.toString(tps)).append(",")
                .append("\"standers\":").append(RequireCareMapping.getRequireCareAsString(this.method));
        cup.append("}");
        return cup.toString();
    }
}
