package com.gos.monitor.client.io;

import com.gos.monitor.common.MonitorSettings;
import com.gos.monitor.common.io.SIO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.UUID;

/**
 * 把方法计时器的计时数据保存起来。
 * Created by XueLiang on 2017-03-29.
 */
public class TimerWriter {
    private final static ByteBuffer[] Buckets = MonitorSettings.Client.Logging ? getBuckets() : new ByteBuffer[]{};
    /**
     * 最近一次写操作发生的时刻
     */
    private static volatile long WriteDiskTime = 0;

    private static final Object Lock = new Object();

    /**
     * 初始化缓存区
     */
    static ByteBuffer[] getBuckets() {
        final int len = 16;
        ByteBuffer[] buffers = new ByteBuffer[len];
        for (int i = 0; i < len; i++) {
            buffers[i] = ByteBuffer.allocateDirect(1024 << 12);
        }
        return buffers;
    }

    static {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                int seconds = 5000;
                while (MonitorSettings.Client.Logging) {
                    waitFor(seconds);
                    if (System.currentTimeMillis() - WriteDiskTime > seconds) {
                        for (ByteBuffer buff : Buckets) {
                            flush(buff);
                        }
                    }
                }
            }

            private void waitFor(long ms) {
                synchronized (Lock) {
                    try {
                        Lock.wait(ms < 0 ? 5000 : ms);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        Thread t = new Thread(r);
        t.setName("Buffer-Flusher");
        t.start();
    }


    /**
     * 把一个字符串写入到Buffer中,,如果Buffer空间不足,则把数据写入到磁盘.
     *
     * @param input
     * @author XueLiang on 2017-03-29
     */
    public static void write(String input) {
        if (input == null) {
            return;
        }
        byte[] bs = input.getBytes(MonitorSettings.UTF8);
        write(bs);
    }

    /**
     * 把一个字节数组写入到Buffer中,,如果Buffer空间不足,则把数据写入到磁盘.
     *
     * @param data
     * @author XueLiang on 2017-03-29
     */
    public static void write(byte[] data) {
        ByteBuffer buffer = select();
        synchronized (buffer) {
            if (buffer.remaining() < data.length) {
                flush(buffer);
            }
            buffer.put(data);
        }
    }

    private static void flush(ByteBuffer buffer) {
        String path = MonitorSettings.Client.TimerFileBase + MonitorSettings.getDataTimeFilePath(Calendar.MINUTE);
        write(path, UUID.randomUUID().toString() + MonitorSettings.Client.TimerFileExtension, buffer);
    }

    /**
     * 把Buffer中的数据写入到磁盘.
     *
     * @author XueLiang on 2017-03-29
     */
    private static void write(String path, String name, ByteBuffer buff) {
        if (path == null) {
            return;
        }
        if (name == null) {
            return;
        }
        if (buff.capacity() - buff.remaining() < 1) {
            return;
        }
        try (FileOutputStream fos = createFileOutputStream(path, name)) {
            if (fos == null) {
                return;
            }
            try (FileChannel channel = fos.getChannel()) {
                synchronized (buff) {
                    buff.flip();
                    channel.write(buff);
                    buff.clear();
                }
            } finally {
                WriteDiskTime = System.currentTimeMillis();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * 选择一个ByteBuffer,尽量让不同的线程写不同的Buffer,
     * 减小写操作的资源竞争.
     *
     * @return
     * @author XueLiang on 2017-03-30
     */
    private static ByteBuffer select() {
        long tid = Thread.currentThread().getId();
        int ix = (int) (tid & (Buckets.length - 1));
        return Buckets[ix];
    }

    /**
     * 创建一个文件输出流
     *
     * @param path 　目录路径
     * @param name 文件名
     * @return FileOutputStream
     */
    private static FileOutputStream createFileOutputStream(String path, String name) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            return new FileOutputStream(path + name);
        } catch (IOException e) {
            SIO.error("在保存调用数据时,创建文件输出流时发生异常.", e);
            return null;
        }
    }
}
