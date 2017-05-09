package com.gos.monitor.client;

import com.gos.monitor.client.component.BasicHttpServer;
import com.gos.monitor.client.component.Register;
import com.gos.monitor.client.transformer.InvokeTraceTransformer;
import com.gos.monitor.common.MonitorSettings;
import com.gos.monitor.common.io.SIO;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

/**
 * Created by xue on 2016-11-16.
 */
public class Premain {

    /**
     * main方法执行之前调用
     *
     * @param args
     * @param inst
     */
    public static void premain(String args, Instrumentation inst) {

        SIO.info("premain 开始执行...");

        printAbstract();

        SIO.info("插件配置:" + MonitorSettings.getString());

        SIO.info("正在启动:" + MonitorSettings.Client.AppName);

        ClassFileTransformer transformer = new InvokeTraceTransformer();

        inst.addTransformer(transformer, true);

        start();

        SIO.info("premain 执行完成...");
    }

    /**
     * 打印参数示例
     */
    private static void printAbstract() {
        SIO.info(
                "使用示例:\n -Dgos.properties.path=${directory}/parameter.properties " +
                        "\n -javaagent:{directory}/gag-is-client.jar\n\n"
        );
    }

    private static final Object Lock = new Object();

    /**
     * 启动本地队列相关线程
     */
    private static void start() {
        //启动httpserver
        final int port = BasicHttpServer.INSTANCE.start();

        //注册到中央服务器
        Register.INSTANCE.registry(port);
    }
}
