package com.gos.monitor.common;

/**
 * Created by xue on 2017-04-12.
 */
public class Waiter {
    public static void waitFor(Object lock, int ms) {
        synchronized (lock) {
            try {
                lock.wait(ms);
            } catch (InterruptedException e) {
            }
        }
    }
}
