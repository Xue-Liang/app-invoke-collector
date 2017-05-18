package com.gos.monitor.client.entity;

import com.gos.monitor.annotation.RequireCare;

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
        StringBuilder cup = new StringBuilder(256);

        long million = nanotime.longValue() / 1_000_000;
        long invoked = times.longValue();
        long errorTotal = exceptions.longValue();
        double errorRate = 0d;
        if (invoked > 0) {
            BigDecimal total = new BigDecimal(invoked);
            BigDecimal error = new BigDecimal(errorTotal);
            errorRate = error.divide(total, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
            errorRate *= 100;
        }
        //计算出平均每次调用耗时(毫秒)
        long everytime = invoked > 0 ? (million / invoked) : 0;
        cup.append("{\"method\":\"").append(method).append("\",")
                .append("\"times\":").append(Long.toString(invoked)).append(",")
                .append("\"errors\":").append(Long.toString(errorTotal)).append(",")
                .append("\"errorate\":").append(Double.toString(errorRate)).append(",")
                .append("\"everytime\":").append(Long.toString(everytime < 1 ? 1 : everytime)).append(",")
                .append("\"standers\":");

        RequireCare mark = RequireCareMapping.get(this.method);
        if (null == mark) {
            cup.append("{}");
        } else {
            cup.append("{")
                    .append("\"name\":\"").append(mark.name())
                    .append("\",")
                    .append("\"description\":\"")
                    .append(mark.description())
                    .append("\",")
                    .append("\"maxAverageTime\":")
                    .append(Integer.toString(mark.maxAverageTime()))
                    .append(",")
                    .append("\"maxError\":")
                    .append(Integer.toString(mark.maxError()))
                    .append("}");
        }
        cup.append("}");
        return cup.toString();
    }
}
