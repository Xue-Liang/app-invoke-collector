package com.gos.monitor.common.http;

/**
 * Created by xue on 2017-04-06.
 */
public class HttpResponse {

    private ResponseStatusLine statusLine;
    private HttpHeader headers;
    private byte[] responseBody;

    public ResponseStatusLine getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(ResponseStatusLine statusLine) {
        this.statusLine = statusLine;
    }

    public HttpHeader getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeader headers) {
        this.headers = headers;
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(byte[] responseBody) {
        this.responseBody = responseBody;
    }

    public static class ResponseStatusLine {
        private String statusLine;
        private String protocol;
        private String protocolVersion;
        private int statusCode;
        private String statusDescription;

        public ResponseStatusLine(String line) {
            this.statusLine = line;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getProtocolVersion() {
            return protocolVersion;
        }

        public void setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

        public String getStatusDescription() {
            return statusDescription;
        }

        public void setStatusDescription(String statusDescription) {
            this.statusDescription = statusDescription;
        }

        public String toString() {
            return this.statusLine;
        }
    }
}
