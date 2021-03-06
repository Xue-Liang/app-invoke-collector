package com.gos.monitor.common;

import com.gos.monitor.common.io.SIO;

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

    public static void notify(Object lock) {
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
