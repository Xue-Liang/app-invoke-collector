package com.gooagoo.monitor.client;

import com.gooagoo.monitor.client.component.BasicHttpServer;
import com.gooagoo.monitor.client.component.Register;
import com.gooagoo.monitor.client.io.DataBagDispatcher;
import com.gooagoo.monitor.client.transformer.InvokeTraceTransformer;
import com.gooagoo.monitor.common.MonitorSettings;
import com.gooagoo.monitor.common.io.SIO;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

/**
 * Created by xue on 2016-11-16.
 */
public class AgentEntry {

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
        if (!MonitorSettings.Client.IsPush) {
            SIO.info("因配置[gos.monitor.switch.push=false],故不启动数据推送线程.");

            //启动httpserver
            final int port = BasicHttpServer.INSTANCE.start();

            //注册到中央服务器
            Register.INSTANCE.start(port);
        } else {
            //step 1.启动数据推送线程
            for (int i = 0; i < 2; i++) {
                String threadName = "DataDispatcher-" + (i + 1);
                SIO.info("正在启动队列消费线程:" + threadName);
                DataBagDispatcher dispatcher = new DataBagDispatcher();
                Thread t = new Thread(dispatcher);
                t.setName(threadName);
                t.start();
            }
        }
    }


}
