package com.gos.monitor.common.http;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by xue on 2017-04-06.
 */
public class HttpConnection implements Closeable {
    private Socket socket;

    private String host;

    private int port;

    public HttpConnection(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.socket = new Socket(host, port);
    }

    public void release() {
        try {
            this.close();
        } catch (Exception e) {
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void close() throws IOException {
        if (this.socket != null) {
            this.socket.close();
        }
    }
}
