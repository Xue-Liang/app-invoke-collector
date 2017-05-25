package com.gos.monitor.common;

import com.gos.monitor.common.io.SIO;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * Created by xue on 2017-03-31.
 */
public class MonitorSettings {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final byte[] LineSeparator = "\n".getBytes(MonitorSettings.UTF8);

    private static Properties SystemProperties = new Properties();

    static {
        String path = System.getProperty("gos.properties.path");
        SIO.info(getDataTime(Calendar.MILLISECOND, "-") + "-加载:" + path + "...");
        if (path == null) {
            path = "/home/gos/agents/parameter.properties";
            System.getProperties().put("gos.properties.path", path);
            SIO.info(getDataTime(Calendar.MILLISECOND, "-") + "-未指定系统属性:[gos.properties.path],系统将使用默认值:[" + path + "]");
        }
        File f = new File(path);
        if (!f.exists()) {
            SIO.info(getDataTime(Calendar.MILLISECOND, "-") + "gos.properties.path=" + path + " 不存在...");
        } else {
            try (InputStream is = new FileInputStream(f)) {
                SystemProperties.load(is);
            } catch (IOException e) {
                SIO.error(getDataTime(Calendar.MILLISECOND, "-") + "-加载:" + path + "时发生异常...", e);
            }
        }
        SIO.info(getDataTime(Calendar.MILLISECOND, "-") + "-加载:" + path + "完成");
    }

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

    public static class Client {
        public static final String AppName = getAppName();

        public static final String AppOwner = getAppOwner();

        public static final String AppOwnerContact = getContact();

        public static final String TempDirectory = getTempDirectory();

        /**
         * class文件被修改后,存放的路径
         */
        public static final String WeavedClassesFileBase = getWeavedClassesFileBase();

        /**
         * 计时器文件扩展名
         */
        public static final String TimerFileExtension = ".timer";

        /**
         * 单元分隔符(unit separator),用于 InvokeTimer 各字段之间的分隔符
         */
        public static final String US = new String(new char[]{(int) 9786});//☺

        /**
         * 用于 InvokeTimer　序列化为字符串时,替换异常信息中的回车、换行符.
         */
        public static final String LRS = new String(new char[]{(int) 9788});//☼

        public static final String LocalIpV4 = getIpByVersion(4);

        public static final String LocalIpV6 = getIpByVersion(6);

        public static final String RegistryServer = getRegistry();

        public static final Pattern IncludePackages = getIncludePackage();

        public static final Pattern ExcludePackages = getExcludePackage();

        public static final boolean Logging = getLogging();

        public static final int Port = getPort();

        //================


        private Client() {
        }

        private static int getPort() {
            String txt = (String) SystemProperties.get("gos.monitor.listen.port");
            if (txt != null) {
                try {
                    return Integer.parseInt(txt.trim());
                } catch (Exception e) {

                }
            }
            return -1;
        }

        private static boolean isStore() {
            //gos.monitor.switch.push
            String txt = (String) SystemProperties.get("gos.monitor.switch.store");
            if (txt != null) {
                try {
                    return Boolean.parseBoolean(txt.trim());
                } catch (Exception e) {

                }
            }
            return false;
        }

        private static String getContact() {
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载Contact...");
            String appOwnerContact = (String) SystemProperties.get("gos.monitor.appOwnerContact");
            if (appOwnerContact != null) {
                appOwnerContact = appOwnerContact.trim();
            } else {
                appOwnerContact = "";
            }
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载Contact完成");
            return appOwnerContact;
        }

        private static String getAppOwner() {
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载Owner...");
            String appOwner = (String) SystemProperties.get("gos.monitor.appOwner");
            if (appOwner != null) {
                appOwner = appOwner.trim();
            } else {
                appOwner = "";
            }
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载Owner完成");
            return appOwner;
        }

        private static String getAppName() {
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载AppName...");
            String appName = (String) SystemProperties.get("gos.monitor.appName");
            if (appName != null) {
                appName = appName.trim();
                if (appName.length() < 1) {
                    appName = "Unknow-App";
                }
            }
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载AppName完成");
            return appName;
        }

        private static boolean getLogging() {
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载Logging...");
            //是否需要记录方法调用轨迹日志
            String txt = (String) SystemProperties.get("gos.monitor.switch.logging");
            boolean logging = false;
            if (txt != null) {
                try {
                    logging = Boolean.parseBoolean(txt);
                } catch (Exception e) {

                }
            }
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载Logging完成");
            return logging;
        }

        private static Pattern getExcludePackage() {
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载ExcludePackage...");
            //不需要监控的包
            String exclude = (String) SystemProperties.get("gos.monitor.exclude.packages");
            String defaultExcludePackages = "(com.*.monitor)|(com.*.container)|(com.*.entity)|(com.*.vo)|(com.*.domain)|(.*.log)";
            Pattern pattern;
            if (exclude == null || (exclude.length() < 1)) {
                pattern = Pattern.compile(defaultExcludePackages, Pattern.CASE_INSENSITIVE);
            } else {
                try {
                    pattern = Pattern.compile(exclude, Pattern.CASE_INSENSITIVE);
                } catch (Exception e) {
                    SIO.info("监控中心-包的正则表达式错误: " + exclude);
                    pattern = Pattern.compile(defaultExcludePackages, Pattern.CASE_INSENSITIVE);
                }
            }
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载ExcludePackage完成");
            return pattern;
        }

        private static Pattern getIncludePackage() {
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载IncludePackage...");
            //需要监控的包
            Pattern pattern;
            String include = (String) SystemProperties.get("gos.monitor.include.packages");
            String defaultIncludePackages = "(com.*.controller)|(com.*.action)|(com.*.service)|(com.*.dao)";
            if (include == null || include.length() < 1) {
                pattern = Pattern.compile(defaultIncludePackages, Pattern.CASE_INSENSITIVE);
            } else {
                try {
                    pattern = Pattern.compile(include, Pattern.CASE_INSENSITIVE);
                } catch (Exception e) {
                    SIO.info("监控中心-包的正则表达式错误: " + include);
                    pattern = Pattern.compile(defaultIncludePackages, Pattern.CASE_INSENSITIVE);
                }
            }
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载IncludePackage完成");
            return pattern;
        }

        private static String getIpByVersion(int version) {
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载ipv" + version + "...");
            // 获取本地IP地址
            String ip = null;
            try {
                Enumeration<NetworkInterface> nets;
                nets = NetworkInterface.getNetworkInterfaces();
                while (nets.hasMoreElements() && (Client.LocalIpV4 == null || Client.LocalIpV6 == null)) {
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
                SIO.info("监控中心-获取本机ip地址时发生异常.");
            }
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载ipv" + version + "完成");
            return ip == null ? "" : ip;
        }

        private static String getRegistry() {
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载registry...");
            // 中央服务器注册接口地址
            String cs = (String) SystemProperties.get("gos.monitor.server.registry");
            if (cs != null) {
                try {
                    URI uri = URI.create(cs);
                    cs = uri.toString();
                } catch (Exception e) {
                    SIO.info("监控中心-URL配置错误,gos.monitor.server.registry＝" + cs + " 不是一个合法的URL");
                }
            }
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载registry完成");
            return cs == null ? "" : cs;
        }

        private static String getTempDirectory() {
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载TempDirectory...");
            // 临时目录路径,该路径用来存放上报失败的那些统计数据
            String tmp = (String) SystemProperties.get("gos.monitor.tmp");
            if (tmp != null && (tmp = tmp.trim()).length() > 0) {
                File dir = new File(tmp);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载TempDirectory完成");
            return tmp == null ? "/tmp/is" : tmp;
        }

        private static String getWeavedClassesFileBase() {
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载WeavedClassesFileBase...");
            boolean hasFileSeparator = TempDirectory != null && (TempDirectory.endsWith("/") || TempDirectory.endsWith("\\"));
            String path;
            if (hasFileSeparator) {
                path = TempDirectory + "client/" + AppName + "/" + "classes/";
            } else {
                path = (TempDirectory == null ? "/tmp/is" : TempDirectory) + "/client/" + AppName + "/classes/";
            }
            SIO.info(MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "-加载WeavedClassesFileBase完成");
            return path;
        }
    }

    public static class Server {

        /**
         * 数据压缩文件所在的基目录
         */
        public static final String DataFileBase = "/tmp/is/server/";
        /**
         * 数据压缩文件的扩展名
         */
        public static final String DataFileExtension = ".gz";
        /**
         * 多个GZIP数据包的分隔符
         */
        public static final byte[] GS = gzip(new String(new char[]{(int) 9787}));//☻

        private static byte[] gzip(final String txt) {
            try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                final byte[] bytes = txt.getBytes(UTF8);
                try (final GZIPOutputStream gos = new GZIPOutputStream(bos)) {
                    gos.write(bytes);
                    return bos.toByteArray();
                }
            } catch (Exception e) {

            }
            return null;
        }
    }


    public static String getString() {
        return SystemProperties != null ? SystemProperties.toString() : "{}";
    }
}
