package com.gos.monitor.common.http;


import java.net.URI;

/**
 * Created by xue on 2017-04-06.
 */
public class HttpRequest {
    private String requestLine = null;
    private String method = null;
    private URI uri = null;
    private HttpHeader header = null;

    private byte[] requestBody = null;

    public enum RequestMethod {
        GET, POST
    }

    public static HttpRequest create(URI uri, RequestMethod method) {
        switch (method.name()) {
            case "GET":
                return new HttpRequest(RequestMethod.GET, uri);
            case "POST":
                return new HttpRequest(RequestMethod.POST, uri);
            default:
                return new HttpRequest(RequestMethod.GET, uri);
        }
    }


    private HttpRequest(RequestMethod rm, URI uri) {
        this.method = rm.name();
        this.uri = uri;
        String query = uri.getRawQuery();
        this.requestLine = rm.name() + " " + uri.getRawPath() + (query == null ? "" : "?" + query) + " HTTP/1.1\r\n";
        header = new HttpHeader();
        header.set("Host", this.uri.getHost());
        header.set("Connection", "close");
        header.set("User-Agent", "Mozilla/5.0");
    }

    public URI getURI() {
        return this.uri;
    }

    public String getMethod() {
        return this.method;
    }

    public void setHttpHeader(String key, String value) {
        this.header.set(key, value);
    }

    public HttpHeader getHttpHeader() {
        return this.header;
    }

    public String getRequestLine() {
        return this.requestLine;
    }

    public void setRequestBody(byte[] data) {
        if (data != null) {
            this.header.set("Content-Length", Integer.toString(data.length));
        }
        this.requestBody = data;
    }

    public byte[] getRequestBody() {
        return this.requestBody;
    }
}
