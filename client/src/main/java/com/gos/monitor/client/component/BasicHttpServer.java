package com.gos.monitor.client.component;

import com.gos.monitor.client.entity.InvokeStatisticsGroup;
import com.gos.monitor.common.MonitorSettings;
import com.gos.monitor.common.StringHelper;
import com.gos.monitor.common.io.SIO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Created by xue on 2017-04-14.
 */
public class BasicHttpServer {

    public static final BasicHttpServer INSTANCE = new BasicHttpServer();

    private final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    private final ServerSocket server = this.createServerSocket();

    private BasicHttpServer() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                SIO.info(Thread.currentThread().getName() + " 程序退出时,正在关闭BasicHttpServer");
                if (BasicHttpServer.INSTANCE.server != null) {
                    try {
                        BasicHttpServer.INSTANCE.EXECUTOR.shutdown();
                    } catch (Exception e) {
                        SIO.error("程序退出时,关闭线程池时发生异常.", e);
                    }
                    try {
                        BasicHttpServer.INSTANCE.server.close();
                        try (Socket socket = new Socket("127.0.0.1", MonitorSettings.Client.Port)) {
                            OutputStream os = socket.getOutputStream();
                            os.write("GET /look/statistics HTTP/1.1\r\n".getBytes(MonitorSettings.UTF8));
                            os.write("Content-Type:application/json;charset=UTF-8\r\n".getBytes(MonitorSettings.UTF8));
                            os.write("Content-Length:0\r\n\r\n".getBytes(MonitorSettings.UTF8));
                            os.flush();
                        } catch (Exception e) {
                            SIO.info("程序退出时,已通知BasicHttpServer执行关闭操作.");
                        }
                    } catch (Exception e) {
                        SIO.error("程序退出时,关闭BasicHttpServer时发生异常.", e);
                    }
                }
                SIO.info(Thread.currentThread().getName() + " 程序退出时,关闭BasicHttpServer完成");
            }
        };
        Thread hook = new Thread(r, "BasicHttpServerShutdownHook-" + server.getLocalSocketAddress().toString());
        Runtime.getRuntime().addShutdownHook(hook);
    }

    public int start() {
        if (this.server == null) {
            SIO.error("监控插件启动失败.可能是因为端口号:[" + MonitorSettings.Client.Port + "]被占用.");
            return -1;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (server != null && !server.isClosed()) {
                    try {
                        Socket socket = server.accept();
                        socket.setKeepAlive(false);
                        Worker worker = new Worker(socket);
                        EXECUTOR.execute(worker);
                    } catch (Exception e) {
                        if (!server.isClosed()) {
                            SIO.error("内置 BasicHttpServer 服务器发生异常.", e);
                        } else {
                            SIO.info("内置 BasicHttpServer 已关闭.");
                        }
                    }
                }
            }
        };
        Thread t = new Thread(r, "BasicHttpServer-" + server.getLocalSocketAddress().toString());
        t.start();
        return this.server.getLocalPort();
    }

    /**
     * 监听一个端口号，用于响应拉取统计信息的请求.
     *
     * @throws IOException
     */

    private static class Worker implements Runnable {

        private Socket socket = null;

        Worker(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            if (this.socket == null || this.socket.isClosed()) {
                return;
            }
            try (final Socket socket = this.socket) {
                InputStream is = this.socket.getInputStream();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line = reader.readLine();
                    if (line != null) {
                        String[] requestLine = StringHelper.split(line, " ", 3);
                        if (requestLine != null && requestLine.length > 1) {
                            String uri = requestLine[1];
                            if ("/pull/statistics".equals(uri != null ? uri.trim() : uri)) {
                                //Response
                                OutputStream os = socket.getOutputStream();
                                pull(os);
                            } else if ("/look/statistics".equals(uri != null ? uri.trim() : uri)) {
                                //Response
                                OutputStream os = socket.getOutputStream();
                                look(os);
                            } else {
                                //Response 404
                                OutputStream os = socket.getOutputStream();
                                this.notFoundResource(os);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                SIO.error("BasicHttpServer网络异常", e);
            }
        }

        /**
         * 查看统计数据的http接口
         *
         * @param os
         * @throws IOException
         */
        private void look(OutputStream os) throws IOException {
            //ResponseStatusLine
            os.write("HTTP/1.1 200 OK\r\n".getBytes(MonitorSettings.UTF8));

            //Response Headers
            os.write(("HOST:" + MonitorSettings.Client.LocalIpV4 + "\r\n").getBytes(MonitorSettings.UTF8));
            os.write(("Content-Type:application/json;charset=UTF-8\r\n").getBytes(MonitorSettings.UTF8));
            os.write(("Connection:close\r\n").getBytes(MonitorSettings.UTF8));

            //Response Body
            String body = InvokeStatisticsGroup.view();
            byte[] data = body.getBytes(MonitorSettings.UTF8);
            os.write(("Content-Length:" + Integer.toString(data.length) + "\r\n").getBytes(MonitorSettings.UTF8));
            os.write("\r\n".getBytes(MonitorSettings.UTF8));

            os.write(data);
            os.flush();
        }

        /**
         * 中央服务器拉取统计数据的http接口
         *
         * @param os
         * @throws IOException
         */
        private void pull(OutputStream os) throws IOException {
            //ResponseStatusLine
            os.write("HTTP/1.1 200 OK\r\n".getBytes(MonitorSettings.UTF8));

            //Response Headers
            os.write(("HOST:" + MonitorSettings.Client.LocalIpV4 + "\r\n").getBytes(MonitorSettings.UTF8));
            os.write(("Content-Type:application/json;charset=UTF-8\r\n").getBytes(MonitorSettings.UTF8));
            os.write(("Connection:close\r\n").getBytes(MonitorSettings.UTF8));

            //Response Body
            InvokeStatisticsGroup.TimeGroup group = InvokeStatisticsGroup.dump();
            String body = group.toString();
            byte[] data = body.getBytes(MonitorSettings.UTF8);
            os.write(("Content-Length:" + Integer.toString(data.length) + "\r\n").getBytes(MonitorSettings.UTF8));
            os.write("\r\n".getBytes(MonitorSettings.UTF8));

            os.write(data);
            os.flush();
        }

        /**
         * 响应404
         *
         * @param os
         * @throws IOException
         */
        private void notFoundResource(OutputStream os) throws IOException {
            //ResponseStatusLine
            os.write("HTTP/1.1 404 NotFound\r\n\r\n".getBytes(MonitorSettings.UTF8));
            os.write("接口不存在.".getBytes());
            os.flush();
        }

    }


    private ServerSocket createServerSocket() {

        int port = MonitorSettings.Client.Port;

        if (port < 1) {
            SIO.error("应用:[" + MonitorSettings.Client.AppName + "] 监听端口:[" + MonitorSettings.Client.Port + "],端口号不合法.");
            return null;
        }

        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
        } catch (Exception e) {
            SIO.error("监听端口:" + port + "时发生了异常,此端口号可能已被占用.", e);
        }
        return server;
    }
}
