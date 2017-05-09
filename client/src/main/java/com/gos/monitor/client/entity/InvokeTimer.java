package com.gos.monitor.client.entity;

import com.gooagoo.monitor.common.MonitorSettings;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

/**
 * 方法执行计时器,记录方法开始执行到方法返回所消耗的时间,单位:纳秒
 * Created by xue on 16-11-23.
 */
public class InvokeTimer implements Serializable {


    /**
     * 方执行时的当前线程id
     */
    private long tid;

    private String stackId;

    private long step;

    private long startTime = 0;

    private long finishTime = 0;

    /**
     * 方法执行的开始时时间 (纳秒)
     */
    private long beginNano = -1;
    /**
     * 方法执行的结束时间(纳秒)
     */
    private long finishNano = -1;
    /**
     * 耗时(纳秒)
     */
    private long elapsed = -1;
    /**
     * 方法名,格式如: com.gooagoo.base.device.impl.DeviceServiceImpl.getDevice(String id)
     */
    private String methodName;
    /**
     * 方法异常堆栈信息,如果没有抛出异常则为null
     */
    private boolean hasException;

    private static final String Empty = "";

    public InvokeTimer(String stackId, String methodName) {
        this.start();
        this.stackId = stackId;
        this.methodName = methodName;
    }

    /**
     * 返回方法开始执行时的时间戳
     *
     * @return
     */
    public long start() {
        this.tid = Thread.currentThread().getId();
        this.beginNano = System.nanoTime();
        return this.startTime = System.currentTimeMillis();
    }

    /**
     * 返回方法结束时的时间戳
     *
     * @return
     */
    public long finish() {
        if (this.elapsed < 1) {
            this.finishNano = System.nanoTime();
            this.elapsed = this.finishNano - this.beginNano;
        }
        return this.finishTime = System.currentTimeMillis();
    }

    public long getElapsed() {
        this.finish();
        return this.elapsed;
    }

    public String getMethodName() {
        return this.methodName;
    }


    public void setHasException(boolean hasExceptions) {
        this.hasException = hasExceptions;
    }

    public boolean getHasException() {
        return this.hasException;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public long getFinishTime() {
        return this.finishTime;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public long getTid() {
        return this.tid;
    }

    public String getStackId() {
        return this.stackId;
    }

    public long getStep() {
        return this.step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    @Override
    public String toString() {
        StringBuilder cup = new StringBuilder(256);
        cup.append(Long.toString(tid, Character.MAX_RADIX))
                .append(MonitorSettings.Client.US)
                .append(this.stackId)
                .append(MonitorSettings.Client.US)
                .append(Long.toString(step, Character.MAX_RADIX))
                .append(MonitorSettings.Client.US)
                .append(methodName)
                .append(MonitorSettings.Client.US)
                .append(Boolean.toString(hasException))
                .append(MonitorSettings.Client.US)
                .append(Long.toString(startTime, Character.MAX_RADIX))
                .append(MonitorSettings.Client.US)
                .append(Long.toString(finishTime, Character.MAX_RADIX))
                .append(MonitorSettings.Client.US)
                .append(Long.toString(this.elapsed, Character.MAX_RADIX))
                .append("\n");
        return cup.toString();
    }
}