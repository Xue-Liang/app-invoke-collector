package com.gos.test;

import com.gos.monitor.annotation.Mark;
import com.gos.monitor.client.entity.MarkMapping;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by xue on 2017-05-11.
 */
public class Tester {


    private static final Executor executor = Executors.newFixedThreadPool(4);

    public static void main(String... args) {
        int max = 4096 << 10;
        for (int i = 0; i < max; i++) {
            write("/tmp/is", Integer.toString(i), new byte[]{});
        }
        //post();
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

    @Mark(name = "平台统一短信发送模块", description = "", maxAverageTime = 5000)
    public static void post() {
        System.out.println("Hello world...");
        System.out.println(MarkMapping.json());
    }

    @Mark(name = "获取方法全名", description = "", maxAverageTime = 2000)
    public static String getFullName(Method m) {
        StringBuilder fullName = new StringBuilder(128);
        fullName.append(m.getDeclaringClass().getName())
                .append(".").append(m.getName())
                .append("(");
        Class<?>[] cs = m.getParameterTypes();
        int i = 0, len = cs.length - 1;
        for (; i < len; i++) {
            fullName.append(cs[i].getSimpleName())
                    .append(",");
        }
        if (i < cs.length) {
            fullName.append(cs[i].getSimpleName());
        }
        fullName.append(")");
        return fullName.toString();
    }

    static void sub() {
        String m = "com.gooagoo.pay.channel.PayBussinessServiceImpl.getPayPermission(LogParam,String,String,List)";
        int ix = m.lastIndexOf('.');
        String c = m.substring(0, ix);
        System.out.println(c);
    }
}
