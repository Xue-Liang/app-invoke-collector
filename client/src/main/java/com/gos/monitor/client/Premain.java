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
        SIO.sout("premain 开始执行...");

        printAbstract(inst);

        SIO.sout("正在启动:" + MonitorSettings.Client.AppName());

        ClassFileTransformer transformer = new InvokeTraceTransformer();

        inst.addTransformer(transformer, inst.isRetransformClassesSupported());

        start();

        SIO.sout("premain 执行完成...");
    }


    /**
     * 打印参数示例
     */
    private static void printAbstract(Instrumentation inst) {
        SIO.sout(
                "\n使用示例:\n 添加如下JVM参数:\n " +
                        "-Dgos.aic=${directory}/aci.properties " +
                        "-javaagent:{directory}/AIC.jar\n\n");
        String support = "支持";
        String unsupport = "不支持";
        SIO.sout("是否支持类重新变型: " + (inst.isRetransformClassesSupported() ? support : unsupport));
        SIO.sout("是否支持类重新定义:" + (inst.isRedefineClassesSupported() ? support : unsupport));
        SIO.sout("是否支持本地方法前辍:" + (inst.isNativeMethodPrefixSupported() ? support : unsupport));
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
