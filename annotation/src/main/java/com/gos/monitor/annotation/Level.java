package com.gos.monitor.annotation;

/**
 * 用于描述接口方法的重要程度
 * Created by xue on 2017-05-18.
 */
public enum Level {
    //低
    Low(0),
    //普通
    Normal(1),
    //高
    High(2);
    private int level;

    Level(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return Integer.toString(level);
    }
}
