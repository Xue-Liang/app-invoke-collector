package com.gos.monitor.common;

import com.gos.monitor.common.io.SIO;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * Created by xue on 2017-03-31.
 */
public class MonitorSettings {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final byte[] LineSeparator = "\n".getBytes(MonitorSettings.UTF8);


    public static final String getDataTime(int unit, String separator) {
        Calendar c = Calendar.getInstance(Locale.PRC);

        switch (unit) {

            case Calendar.YEAR:
                int year = c.get(Calendar.YEAR);
                return Integer.toString(year) + separator;
            case Calendar.MONTH:
                year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH) + 1;
                return Integer.toString(year) + separator +
                        Integer.toString(month) + separator;

            case Calendar.DAY_OF_MONTH:
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH) + 1;
                int day = c.get(Calendar.DAY_OF_MONTH);
                return Integer.toString(year) + separator +
                        Integer.toString(month) + separator +
                        Integer.toString(day);
            case Calendar.HOUR_OF_DAY:
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH) + 1;
                day = c.get(Calendar.DAY_OF_MONTH);
                int hour = c.get(Calendar.HOUR_OF_DAY);
                return Integer.toString(year) + separator +
                        Integer.toString(month) + separator +
                        Integer.toString(day) + separator +
                        Integer.toString(hour);
            case Calendar.MINUTE:
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH) + 1;
                day = c.get(Calendar.DAY_OF_MONTH);
                hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                return Integer.toString(year) + separator +
                        (month < 10 ? "0" : "") + Integer.toString(month) + separator +
                        (day < 10 ? "0" : "") + Integer.toString(day) + separator +
                        (hour < 10 ? "0" : "") + Integer.toString(hour) + separator +
                        (minute < 10 ? "0" : "") + Integer.toString(minute);
            case Calendar.SECOND:
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH) + 1;
                day = c.get(Calendar.DAY_OF_MONTH);
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
                int second = c.get(Calendar.SECOND);
                return Integer.toString(year) + separator +
                        (month < 10 ? "0" : "") + Integer.toString(month) + separator +
                        (day < 10 ? "0" : "") + Integer.toString(day) + separator +
                        (hour < 10 ? "0" : "") + Integer.toString(hour) + separator +
                        (minute < 10 ? "0" : "") + Integer.toString(minute) + separator +
                        (second < 10 ? "0" : "") + Integer.toString(second);
            default:
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH) + 1;
                day = c.get(Calendar.DAY_OF_MONTH);
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
                second = c.get(Calendar.SECOND);
                int ms = c.get(Calendar.MILLISECOND);
                return Integer.toString(year) + separator +
                        (month < 10 ? "0" : "") + Integer.toString(month) + separator +
                        (day < 10 ? "0" : "") + Integer.toString(day) + separator +
                        (hour < 10 ? "0" : "") + Integer.toString(hour) + separator +
                        (minute < 10 ? "0" : "") + Integer.toString(minute) + separator +
                        (second < 10 ? "0" : "") + Integer.toString(second) + separator +
                        (ms < 10 ? "0" : "") + Integer.toString(ms);
        }
    }

    public static final ClientConstant Client = new ClientConstant();

    public static class ClientConstant {

        private Properties AppParameters = new Properties();
        /**
         * 单元分隔符(unit separator),用于 InvokeTimer 各字段之间的分隔符
         */
        public static final String US = new String(new char[]{(int) 9786});//☺

        ClientConstant() {
            String path = System.getProperty("gos.aic");
            SIO.sout("配置文件:" + path);
            if (path == null) {
                SIO.sout("未指定监控插件配置文件路径,程序退出.");
                System.exit(1);
            }
            File f = new File(path);
            if (!f.exists()) {
                SIO.sout("文件:" + path + "不存在,程序退出.");
                System.exit(1);
            }
            try (InputStream is = new FileInputStream(f)) {
                AppParameters.load(is);
            } catch (IOException e) {
                System.exit(1);
            }
        }

        public String AppName() {
            return AppParameters.getProperty("app.name");
        }

        public String AppOwner() {
            return AppParameters.getProperty("app.owner");
        }

        public String AppOwnerContact() {
            return AppParameters.getProperty("app.owner.contact");
        }

        public String TempDirectory() {
            return AppParameters.getProperty("base.directory");
        }

        /**
         * class文件被修改后,存放的路径
         */
        public String WeavedClassesFileBase = TempDirectory() + "client/" + AppName() + "/" + "classes/";


        public final String LocalIpV4 = getIpByVersion(4);

        public final String LocalIpV6 = getIpByVersion(6);

        public String RegistryServer() {
            return AppParameters.getProperty("server.registry");
        }

        private Pattern IncludePattern = null;

        public Pattern IncludePackages() {
            if (IncludePattern == null)
                IncludePattern = Pattern.compile(AppParameters.getProperty("rule.include.packages"));
            return IncludePattern;
        }

        private Pattern ExcludePattern = null;

        public Pattern ExcludePackages() {
            if (ExcludePattern == null)
                ExcludePattern = Pattern.compile(AppParameters.getProperty("rule.exclude.packages"));
            return ExcludePattern;
        }

        private Boolean Logging = false;

        public boolean Logging() {
            if (Logging == null) {
                return Logging = Boolean.parseBoolean(AppParameters.getProperty("switch.logging"));
            }
            return Logging;
        }

        private Integer port = null;

        public Integer Port() {
            if (port == null) {
                port = Integer.parseInt(AppParameters.getProperty("http.listen.port"));
            } else {
                port = 9707;
            }
            return port;
        }

        //================


        private static String getIpByVersion(int version) {
            // 获取本地IP地址
            String ip = null;
            try {
                Enumeration<NetworkInterface> nets;
                nets = NetworkInterface.getNetworkInterfaces();
                while (nets.hasMoreElements()) {
                    NetworkInterface net = nets.nextElement();
                    Enumeration<InetAddress> addresses = net.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress()) {
                            if (address instanceof Inet4Address) {
                                if (version == 4) {
                                    ip = address.getHostAddress();
                                    break;
                                }
                            } else if (address instanceof Inet6Address) {
                                if (version == 6) {
                                    ip = address.getHostAddress();
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                SIO.sout("监控中心-获取本机ip地址时发生异常.", e);
            }
            return ip == null ? "" : ip;
        }
    }
}
