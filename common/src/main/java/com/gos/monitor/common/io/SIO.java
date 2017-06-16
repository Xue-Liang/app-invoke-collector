package com.gos.monitor.common.io;


import com.gos.monitor.common.MonitorSettings;
import com.gos.monitor.common.Waiter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.UUID;

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

    public static void sout(String message) {
        sout(message, null);
    }

    public static void sout(String message, Throwable t) {
        long tid = Thread.currentThread().getId();
        String line = MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + " 监控插件-" + " tid: [" + Long.toString(tid) + ",0x" + Long.toString(tid, 16) + "] " + message;
        System.out.println(line);
        if (t != null) {
            t.printStackTrace(System.out);
        }
    }

    private static void output(String message, Throwable t, String level) {
        if (MonitorSettings.Client.Logging()) {
            long tid = Thread.currentThread().getId();
            String line = MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + " 监控插件-" + (level == null ? "" : level) + " tid: [" + Long.toString(tid) + ",0x" + Long.toString(tid, 16) + "] " + message;
            LogBuffer.INSTANCE.write(line, t);
        }
    }

    private static class LogBuffer {
        private static final int MaxBuckets = 32;
        private static final int BucketSize = 1024 << 11;
        private static final ByteBuffer[] Buckets = new ByteBuffer[MaxBuckets];
        private static final String LogFileDirectory = MonitorSettings.Client.TempDirectory() + File.separator + "client" + File.separator + "log" + File.separator;
        private static final String LogFileName = MonitorSettings.Client.AppName() + "-" + MonitorSettings.Client.Port() + ".log";
        private static FileOutputStream FOS = getFileOutputStream();
        private static final Object BufferLock = new Object();
        private static volatile boolean ExitFlusher = false;
        private static volatile long WriteFileTime = 0;

        static {
            if (MonitorSettings.Client.Logging()) {
                for (int i = 0; i < MaxBuckets; i++) {
                    Buckets[i] = ByteBuffer.allocateDirect(BucketSize);
                }

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        ExitFlusher = true;
                        Waiter.notify(BufferLock);
                    }
                };

                Thread hook = new Thread(r);
                hook.setName("SIO-LogBuffer-Hook");
                Runtime.getRuntime().addShutdownHook(hook);


                final Runnable runnableFlusher = new Runnable() {
                    @Override
                    public void run() {
                        do {
                            for (int i = 0; i < MaxBuckets; i++) {
                                ByteBuffer buffer = Buckets[i];
                                if (buffer.position() > 0) {
                                    if (System.currentTimeMillis() - WriteFileTime > 5000) {
                                        LogBuffer.INSTANCE.dump(buffer);
                                    }
                                }
                            }
                            Waiter.waitFor(BufferLock, 5000);
                        } while (!ExitFlusher);
                    }
                };
                Thread flusher = new Thread(runnableFlusher, "SIO-LogBuffer-Flusher");
                flusher.start();
            }
        }

        public static final LogBuffer INSTANCE = new LogBuffer();

        private LogBuffer() {
        }


        private void write(String message, Throwable t) {
            if (message != null) {
                write(message.getBytes(MonitorSettings.UTF8));
            }
            if (t != null) {
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream(1024)) {
                    try (PrintStream stream = new PrintStream(bos, true, MonitorSettings.UTF8.name())) {
                        t.printStackTrace(stream);
                        write(bos.toByteArray());
                    }
                } catch (IOException e) {
                }
            }
        }

        private void write(byte[] data) {
            ByteBuffer buff = select();
            int size = buff.remaining() - (data.length + MonitorSettings.LineSeparator.length);
            synchronized (buff) {
                if (size < 1) {
                    dump(buff);
                }
                buff.put(data);
                buff.put(MonitorSettings.LineSeparator);
            }
        }

        private ByteBuffer select() {
            int ix = (int) (Thread.currentThread().getId() & (MaxBuckets - 1));
            ByteBuffer buff = Buckets[ix];
            return buff;
        }

        private void dump(ByteBuffer buff) {
            try {
                synchronized (FOS) {
                    FileChannel fc = FOS.getChannel();
                    fc.write((ByteBuffer) buff.flip());
                    if (fc.size() >= BucketSize) {
                        fc.close();
                        FOS.close();
                        LogBuffer.INSTANCE.move(getLogFile(), getHistoryFile());
                        FOS = getFileOutputStream();
                    }
                    buff.clear();
                    WriteFileTime = System.currentTimeMillis();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean move(File from, File to) {
            if (from == null || to == null) {
                return false;
            }
            if (!from.exists()) {
                return false;
            }
            if (to.exists()) {
                return false;
            }
            Path src = from.toPath();
            Path target = to.toPath();
            boolean ok = false;
            try {
                ok = target.equals(Files.move(src, target, StandardCopyOption.REPLACE_EXISTING));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ok;
        }


        private static FileOutputStream getFileOutputStream() {
            File directory = new File(LogFileDirectory);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    return null;
                }
            }

            FileOutputStream fos = null;
            File logFile = getLogFile();
            try {
                fos = new FileOutputStream(logFile, true);
            } catch (Exception e) {

            }

            return fos;
        }

        private static File getLogFile() {
            return new File(LogFileDirectory + LogFileName);
        }

        private static File getHistoryFile() {
            String path = LogFileDirectory + MonitorSettings.getDataTime(Calendar.DAY_OF_MONTH, File.separator);
            File directory = new File(path);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    return null;
                }
            }
            return new File(path + File.separator + LogFileName + "_" + MonitorSettings.getDataTime(Calendar.MILLISECOND, "-") + "_" + UUID.randomUUID());
        }
    }
}
