package com.gos.monitor.client.io;


import com.gooagoo.monitor.common.MonitorSettings;
import com.gooagoo.monitor.common.io.SIO;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.ContentEncodingHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URI;
import java.util.zip.GZIPOutputStream;

/**
 * Created by xue on 2016-12-05.
 */
public class DataBagDispatcher implements Runnable {
    private static volatile boolean hasExit = false;

    private static final int MaxDepth = 16;

    public static final Object Lock = new Object();

    public static void waitFor(int seconds) {
        try {
            SIO.info("[调用采集器]-队列消费线程:" + " waiting...");
            synchronized (Lock) {
                Lock.wait(1000 * seconds);
            }
        } catch (InterruptedException e) {
            SIO.info("[调用采集器]-线程挂起时发生异常.");
            e.printStackTrace(System.out);
        }
    }

    public static void main(String... args) {
        File dir = new File("/home/xue/test");
        DataBagDispatcher dis = new DataBagDispatcher();
        dis.dispatch(dir, 1);
    }

    @Override
    public void run() {
        while (!hasExit) {
            File dir = new File(MonitorSettings.Client.TimerFileBase);
            dispatch(dir, 1);
        }
    }

    private void dispatch(File f, int depth) {
        if (depth > MaxDepth) {
            return;
        } else if (depth < 0) {
            return;
        }
        if (f == null) {
            return;
        }
        if (!f.exists()) {
            return;
        }

        if (!f.canRead()) {
            return;
        }
        if (f.isFile()) {
            if (!f.getName().endsWith(MonitorSettings.Client.TimerFileExtension)) {
                f.delete();
                return;
            }
            try (final FileInputStream fis = new FileInputStream(f)) {
                final ByteArrayOutputStream gzip = new ByteArrayOutputStream(fis.available());
                try (final GZIPOutputStream gos = new GZIPOutputStream(gzip, 1024 << 8, false)) {
                    gos.write(getHeader());
                    byte[] dst = new byte[1024 << 8];
                    int size;
                    while ((size = fis.read(dst)) > 0) {
                        gos.write(dst, 0, size);
                    }
                }

                byte[] bytes = gzip.toByteArray();
                SIO.info("正在发送文件:[" + f.getAbsolutePath() + "],压缩后:" + bytes.length + "bytes");
                long nano = System.nanoTime();
                HttpResponse resp = postByApacheHttpClient(bytes);
                if (resp != null) {
                    if (resp.getStatusLine().getStatusCode() == 200) {
                        f.delete();
                    }
                }
                nano = System.nanoTime() - nano;
                SIO.info("完成发送文件:[" + f.getAbsolutePath() + "],发送了:" + bytes.length + "bytes,耗时约:" + (nano / 1000_000) + "ms");
            } catch (FileNotFoundException e) {
                SIO.info("不存在文件:[" + f.getAbsolutePath() + "].");
            } catch (Throwable e) {
                SIO.info("读取文件失败:[" + f.getAbsolutePath() + "].");
                e.printStackTrace(System.out);
            }
        } else if (f.isDirectory()) {
            File[] fs = f.listFiles();
            if (fs != null && fs.length < 1 && depth > 1) {
                f.delete();
                return;
            }
            for (File item : fs) {
                dispatch(item, depth + 1);
            }
        }
    }

    /**
     * 当数据写入到文件时，在文件开始位置写入此信息
     *
     * @return
     */
    private static byte[] getHeader() {
        return (MonitorSettings.Client.LocalIpV4
                + MonitorSettings.Client.US + MonitorSettings.Client.AppName
                + MonitorSettings.Client.US + MonitorSettings.Client.AppOwner
                + MonitorSettings.Client.US + MonitorSettings.Client.AppOwnerContact + "\n").getBytes(MonitorSettings.UTF8);
    }

    private static ContentEncodingHttpClient ApacheHttpClient = new ContentEncodingHttpClient(new ThreadSafeClientConnManager(), null);

    HttpResponse postByApacheHttpClient(byte[] data) {
        if (data == null || data.length < 1) {
            return null;
        }
        HttpResponse resp = null;
        HttpPost post = new HttpPost();
        HttpEntity entity = null;
        try {
            entity = new ByteArrayEntity(data);
        } catch (Exception e) {
            SIO.info("[调用采集器]-发送数据之前,构造StringEntity对象时发生异常.");
            e.printStackTrace(System.out);
        }
        post.setEntity(entity);
        post.setURI(URI.create(MonitorSettings.Client.CenterServer));
        try {
            resp = ApacheHttpClient.execute(post);
            if (resp.getStatusLine().getStatusCode() != 200) {
                SIO.info("[调用采集器]-中央服务器响应状态行: " + resp.getStatusLine().toString() + "\n");
            }
            try {
                EntityUtils.consume(resp.getEntity());
            } catch (final IOException ignore) {
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return resp;
    }

    public static void exit() {
        synchronized (Lock) {
            hasExit = true;
        }
    }

}
