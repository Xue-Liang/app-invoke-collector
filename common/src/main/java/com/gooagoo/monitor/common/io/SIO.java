package com.gooagoo.monitor.common.io;


import java.util.Calendar;
import java.util.Locale;

/**
 * Created by xue on 2017-04-14.
 */
public class SIO {
    public static void info(String message) {
        output(message, null, "info");
    }

    public static void info(String message, Throwable t) {
        output(message, t, "info");
    }

    public static void warn(String message) {
        output(message, null, "warn");
    }

    public static void warn(String message, Throwable t) {
        output(message, t, "warn");
    }

    public static void error(String message, Throwable t) {
        output(message, t, "error");
    }

    public static void error(String message) {
        output(message, null, "error");
    }


    private static void output(String message, Throwable t, String level) {
        long tid = Thread.currentThread().getId();
        System.out.println(now() + " 监控插件-" + (level == null ? "" : level) + " ThreadId: [0x" + (Long.toString(tid, 16)) + "," + Long.toString(tid) + "] " + message);
        if (t != null) {
            t.printStackTrace(System.out);
        }
    }

    private static String now() {
        Calendar calendar = Calendar.getInstance(Locale.PRC);
        return calendar.get(calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1)
                + "-" + calendar.get(Calendar.DAY_OF_MONTH) + " "
                + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":"
                + calendar.get(Calendar.SECOND) + "." + calendar.get(Calendar.MILLISECOND);
    }
}
