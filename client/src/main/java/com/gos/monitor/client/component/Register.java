package com.gos.monitor.client.component;

import com.gos.monitor.common.MonitorSettings;
import com.gos.monitor.common.Waiter;
import com.gos.monitor.common.http.HttpClient;
import com.gos.monitor.common.http.HttpRequest;
import com.gos.monitor.common.http.HttpResponse;
import com.gos.monitor.common.io.SIO;

import javax.management.monitor.Monitor;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * Created by xue on 2017-04-14.
 */
public class Register {

    public static final Register INSTANCE = new Register();

    private static final Object Lock = new Object();


    private Register() {

    }

    public void registry(final int port) {
        if (port < 1) {
            SIO.error("监控插件未请求中央服务器进行注册.可能是因为本地端口号:[" + MonitorSettings.Client.Port + "]被占用.");
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                URI uri = URI.create(MonitorSettings.Client.RegistryServer);
                HttpRequest req = HttpRequest.create(uri, HttpRequest.RequestMethod.POST);
                req.setHttpHeader("Host", uri.getHost() + ":" + uri.getPort());
                req.setHttpHeader("Content-Type", "applicationn/json;charset=UTF-8");
                req.setHttpHeader("connection", "close");
                String json = "{" +
                        "\"app\": \"" + MonitorSettings.Client.AppName + "\"," +
                        "\"owner\":\"" + MonitorSettings.Client.AppOwner + "\"," +
                        "\"contact\":" + MonitorSettings.Client.AppOwnerContact + "\"," +
                        "\"interface:{"+
                        "\"pull\":\"http://" + MonitorSettings.Client.LocalIpV4 + ":" + port + "/pull/statistics\"," +
                        "\"look\":\"http://" + MonitorSettings.Client.LocalIpV4 + ":" + port + "/look/statistics\"}" +
                        "}";
                req.setRequestBody(json.getBytes(Charset.forName("UTF-8")));
                boolean registry;
                do {
                    long nano = System.nanoTime();
                    try {
                        HttpResponse resp = HttpClient.execute(req);

                        registry = (resp != null) && (resp.getStatusLine().getStatusCode() == 200);

                        if (resp != null && resp.getResponseBody() != null) {
                            SIO.info(new String(resp.getResponseBody(), MonitorSettings.UTF8));
                        }
                    } catch (Exception e) {
                        registry = false;
                        SIO.error("插件注册到:[" + MonitorSettings.Client.RegistryServer + "]时发生了异常.", e);
                    }
                    SIO.info("插件注册到:[" + MonitorSettings.Client.RegistryServer + "]" + (registry ? "成功" : "失败") + "耗时:" + (System.nanoTime() - nano) / 1_000_000 + " ms");

                    if (!registry) {
                        Waiter.waitFor(Lock, 30000);
                    }
                } while (!registry);
            }
        };

        Thread t = new Thread(r);
        t.setName("RegistryThread-" + MonitorSettings.Client.RegistryServer);
        t.start();
    }
}
