package com.gos.monitor.client.component;

import com.gos.monitor.client.collection.InvokeStatisticsBucket;
import com.gos.monitor.common.MonitorSettings;
import com.gos.monitor.common.StringHelper;
import com.gos.monitor.common.io.SIO;

import javax.management.monitor.MonitorSettingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xue on 2017-04-14.
 */
public class BasicHttpServer {

    public static final BasicHttpServer INSTANCE = new BasicHttpServer();

    private final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    private static final ServerSocket HttpServer = createServerSocket();

    static {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                SIO.info(Thread.currentThread().getName() + " 程序退出时,正在关闭BasicHttpServer");
                if (BasicHttpServer.INSTANCE.HttpServer != null) {

                    BasicHttpServer.INSTANCE.EXECUTOR.shutdown();

                    try {
                        BasicHttpServer.INSTANCE.HttpServer.close();
                    } catch (IOException e) {

                    }
                    try (Socket socket = new Socket("127.0.0.1", MonitorSettings.Client.Port)) {
                        OutputStream os = socket.getOutputStream();
                        os.write("GET /look/statistics HTTP/1.1\r\n".getBytes(MonitorSettings.UTF8));
                        os.write("Content-Type:application/json;charset=UTF-8\r\n".getBytes(MonitorSettings.UTF8));
                        os.write("Content-Length:0\r\n\r\n".getBytes(MonitorSettings.UTF8));
                        os.flush();
                    } catch (IOException e) {
                        SIO.info("程序退出时,已通知BasicHttpServer执行关闭操作.");
                    }
                }
                SIO.info(Thread.currentThread().getName() + " 程序退出时,关闭BasicHttpServer完成");
            }
        };
        Thread hook = new Thread(r, "BasicHttpServerShutdownHook-" + HttpServer.getLocalSocketAddress().toString());
        Runtime.getRuntime().addShutdownHook(hook);
    }

    private BasicHttpServer() {

    }

    public int start() {
        if (this.HttpServer == null) {
            SIO.error("监控插件启动失败.可能是因为端口号:[" + MonitorSettings.Client.Port + "]被占用.");
            return -1;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (HttpServer != null && !HttpServer.isClosed()) {
                    try {
                        Socket socket = this.getSocket();
                        Worker worker = new Worker(socket);
                        EXECUTOR.execute(worker);
                    } catch (Exception e) {
                        if (!HttpServer.isClosed()) {
                            SIO.error("内置 BasicHttpServer 服务器发生异常.", e);
                        } else {
                            SIO.info("内置 BasicHttpServer 已关闭.");
                        }
                    }
                }
            }

            private Socket getSocket() throws IOException {
                Socket so = HttpServer.accept();
                so.setSendBufferSize(1024 << 5);
                so.setSoLinger(true, 3);
                so.setSoTimeout(2000);
                so.setKeepAlive(false);
                so.setTcpNoDelay(true);
                return so;
            }
        };
        Thread t = new Thread(r, "BasicHttpServer-" + HttpServer.getLocalSocketAddress().toString());
        t.start();
        return this.HttpServer.getLocalPort();
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
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, MonitorSettings.UTF8))) {
                    String line = reader.readLine();
                    if (line != null) {
                        String[] requestLine = StringHelper.split(line, " ", 3);
                        if (requestLine != null && requestLine.length > 1) {
                            String uri = requestLine[1];
                            if ("/pull/statistics".equals(uri.trim())) {
                                //Response
                                OutputStream os = socket.getOutputStream();
                                pull(os);
                            } else if ("/look/statistics".equals(uri.trim())) {
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
            byte[] responseStatusLineBytes = "HTTP/1.1 200 OK\r\n".getBytes(MonitorSettings.UTF8);

            //Response Headers
            StringBuilder headers = new StringBuilder(512);
            headers.append("HOST:").append(MonitorSettings.Client.LocalIpV4).append("\r\n");
            headers.append("Content-Type:application/json;charset=UTF-8\r\n");
            headers.append("Connection:close\r\n");

            //Response Body
            String body = InvokeStatisticsBucket.view();

            byte[] bodyBytes = body.getBytes(MonitorSettings.UTF8);
            headers.append("Content-Length:").append(Integer.toString(bodyBytes.length)).append("\r\n\r\n");

            byte[] headerBytes = headers.toString().getBytes(MonitorSettings.UTF8);

            this.response(os, responseStatusLineBytes, headerBytes, bodyBytes);

        }

        /**
         * 中央服务器拉取统计数据的http接口
         *
         * @param os
         * @throws IOException
         */
        private void pull(OutputStream os) throws IOException {
            //ResponseStatusLine
            byte[] responseStatusLineBytes = "HTTP/1.1 200 OK\r\n".getBytes(MonitorSettings.UTF8);

            //Response Headers
            StringBuilder headers = new StringBuilder(512);
            headers.append("HOST:").append(MonitorSettings.Client.LocalIpV4).append("\r\n");
            headers.append("Content-Type:application/json;charset=UTF-8\r\n");
            headers.append("Connection:close\r\n");

            //Response Body
            InvokeStatisticsBucket.TimeGroup group = InvokeStatisticsBucket.dump();
            String body = group.toString();
            byte[] bodyBytes = body.getBytes(MonitorSettings.UTF8);
            headers.append("Content-Length:").append(Integer.toString(bodyBytes.length)).append("\r\n\r\n");

            byte[] headerBytes = headers.toString().getBytes(MonitorSettings.UTF8);

            this.response(os, responseStatusLineBytes, headerBytes, bodyBytes);

        }

        /**
         * 响应404
         *
         * @param os
         * @throws IOException
         */
        private void notFoundResource(OutputStream os) throws IOException {
            //ResponseStatusLine
            byte[] responseStatusLineBytes = "HTTP/1.1 404 NotFound\r\n".getBytes(MonitorSettings.UTF8);

            //Response Headers
            StringBuilder headers = new StringBuilder(512);
            headers.append("HOST:").append(MonitorSettings.Client.LocalIpV4).append("\r\n");
            headers.append("Content-Type:text/html;charset=UTF-8\r\n");
            headers.append("Connection:close\r\n");

            //ResponseBody
            byte[] bodyBytes = "接口不存在.".getBytes(MonitorSettings.UTF8);
            headers.append("Content-Length:").append(Integer.toString(bodyBytes.length)).append("\r\n\r\n");

            byte[] headerBytes = headers.toString().getBytes(MonitorSettings.UTF8);

            this.response(os, responseStatusLineBytes, headerBytes, bodyBytes);

        }

        private void response(OutputStream os, byte[] responseStatusLine, byte[] headers, byte[] body) throws IOException {
            os.write(responseStatusLine);
            os.write(headers);
            os.write(body);
            os.flush();
        }
    }


    private static ServerSocket createServerSocket() {

        int port = MonitorSettings.Client.Port;

        if (port < 1) {
            SIO.error("应用:[" + MonitorSettings.Client.AppName + "] 监听端口:[" + MonitorSettings.Client.Port + "],端口号不合法.");
            return null;
        }

        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            server.setReceiveBufferSize(1024);
        } catch (Exception e) {
            SIO.error("监听端口:" + port + "时发生了异常,此端口号可能已被占用.", e);
        }
        return server;
    }
}
