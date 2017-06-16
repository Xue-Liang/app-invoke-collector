package com.gos.monitor.common.http;


import com.gos.monitor.common.MonitorSettings;
import com.gos.monitor.common.StringHelper;
import com.gos.monitor.common.http.HttpResponse.ResponseStatusLine;

import java.io.*;

/**
 * Created by xue on 2017-04-06.
 */
public class HttpClient {
    public static HttpResponse execute(HttpRequest req) throws IOException {
        try (HttpConnection conn = HttpConnectionFactory.getHttpConnection(req.getURI())) {
            OutputStream os = conn.getSocket().getOutputStream();
            byte[] requestLine = req.getRequestLine().getBytes(MonitorSettings.UTF8);
            os.write(requestLine);
            byte[] requestHeaders = req.getHttpHeader().toString().getBytes(MonitorSettings.UTF8);
            os.write(requestHeaders);

            os.write("\r\n".getBytes(MonitorSettings.UTF8));

            byte[] body = req.getRequestBody();
            if (body != null) {
                os.write(body);
            }

            os.flush();


            HttpResponse resp = new HttpResponse();
            InputStream is = conn.getSocket().getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, MonitorSettings.UTF8));

            //step 2.1 解析响应状态行
            String line = reader.readLine();
            if (line == null) {
                return null;
            }
            String[] parts = StringHelper.split(line, " ", 3);

            if (parts.length == 3) {
                ResponseStatusLine statusLine = new ResponseStatusLine(line);
                String txt = parts[0];
                String[] protocol = txt.split("/");
                if (protocol.length == 2) {
                    statusLine.setProtocol(protocol[0]);
                    statusLine.setProtocolVersion(protocol[1]);
                }
                statusLine.setStatusCode(Integer.parseInt(parts[1]));
                statusLine.setStatusDescription(parts[2]);
                resp.setStatusLine(statusLine);
            }
            //step 2.2 解析响应报文头
            HttpHeader responseHeaders = new HttpHeader();
            while ((line = reader.readLine()) != null && line.length() > 0) {
                int ix = line.indexOf(":");
                if (ix > 0) {
                    String key = line.substring(0, ix);
                    String value = line.substring(ix + 1);
                    responseHeaders.set(key, value);
                }
            }
            resp.setHeaders(responseHeaders);

            //step 2.3 读取响应正文
            ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
            int size;
            byte[] buff = new byte[1024];
            while ((size = is.read(buff)) > 0) {
                bos.write(buff, 0, size);
            }
            resp.setResponseBody(bos.toByteArray());
            resp.setResponseBody(bos.toByteArray());
            return resp;
        }
    }
}