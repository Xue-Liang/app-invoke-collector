package com.gooagoo.monitor.client.entity;

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
        long average = invoked > 0 ? (million / invoked) : 0;
        cup.append("{\"method\":\"").append(method).append("\",")
                .append("\"times\":").append(times.longValue()).append(",")
                .append("\"errors\":").append(exceptions.longValue()).append(",")
                .append("\"error_rate\":").append(Double.toString(errorRate)).append(",")
                .append("\"ms_per_invoke\":").append(Long.toString(average < 1 ? 1 : average))
                .append("}");
        return cup.toString();
    }
}
