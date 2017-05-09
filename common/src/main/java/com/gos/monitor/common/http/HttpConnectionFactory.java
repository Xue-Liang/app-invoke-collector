package com.gos.monitor.common.http;


import java.io.IOException;
import java.net.URI;

/**
 * Created by xue on 2017-04-06.
 */
public class HttpConnectionFactory {
    public static HttpConnection getHttpConnection(URI uri) throws IOException {
        String host = uri.getHost();
        int port = uri.getPort();
        port = port < 1 ? 80 : port;
        return new HttpConnection(host, port);
    }
}
