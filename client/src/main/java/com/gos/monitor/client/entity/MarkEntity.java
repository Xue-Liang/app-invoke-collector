package com.gos.monitor.client.entity;

import java.io.Serializable;

/**
 * Created by xue on 2017-05-11.
 */
public class MarkEntity implements Serializable {
    /**
     * 方法的可理解的名称.
     *
     * @return
     */
    String name;

    /**
     * 方法的简述
     *
     * @return
     */
    String description;

    /**
     * 允许方法的平均耗时最长时间(单位:毫秒)
     * 超过此值，发出警报.
     *
     * @return
     */
    int maxAverageTime;

    /**
     * 　在一段时间内最多允许出现的错误数.
     * 　超过此值,发出警报.
     */
    int maxError;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxAverageTime() {
        return maxAverageTime;
    }

    public void setMaxAverageTime(int maxAverageTime) {
        this.maxAverageTime = maxAverageTime;
    }

    public int getMaxError() {
        return maxError;
    }

    public void setMaxError(int maxError) {
        this.maxError = maxError;
    }


}
