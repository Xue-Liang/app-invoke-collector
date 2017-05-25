package com.gos.test;

import com.gos.monitor.annotation.RequireCare;
import com.gos.monitor.client.collection.ObjectChain;
import com.gos.monitor.client.entity.RequireCareMapping;
import com.gos.monitor.common.io.SIO;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by xue on 2017-05-11.
 */
public class Tester {
    static int x;


    private static final Executor executor = Executors.newFixedThreadPool(4);

    public static void main(String... args) throws InvocationTargetException, IllegalAccessException {
        ObjectChain<Integer> chain = new ObjectChain<>();
        for (int i = 0; i < 10; i++) {
            chain.push(i);
        }

        for (; chain.hasMore(); ) {
            System.out.println(chain.pop());
        }

    }


    static void write(final String path, final String name, final byte[] data) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                writeFile(path, name, data);
            }
        };
        executor.execute(r);
    }

    static void writeFile(String path, String name, byte[] data) {
        Thread t = Thread.currentThread();
        System.out.println(t.getName() + " - " + t.getId() + " - " + path + File.separator + name);
    }

    private static class MieMie {

        @RequireCare(name = "平台统一短信发送模块")
        public static void post() {
            System.out.println("Hello world...");
        }

        @RequireCare(name = "根据方法全名打印方法名")
        static void sub() {
            String m = "com.gooagoo.Tester.MieMie.sub(Method)";
            System.out.println(m);
        }
    }
}
