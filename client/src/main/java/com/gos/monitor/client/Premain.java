package com.gos.monitor.client;

import com.gos.monitor.client.component.BasicHttpServer;
import com.gos.monitor.client.component.Register;
import com.gos.monitor.client.transformer.InvokeTraceTransformer;
import com.gos.monitor.common.MonitorSettings;
import com.gos.monitor.common.io.SIO;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.UUID;

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

        printAbstract(inst);

        SIO.info("插件配置:" + MonitorSettings.getString());

        SIO.info("正在启动:" + MonitorSettings.Client.AppName);

        String id = UUID.randomUUID().toString();
        Class<?>[] loadedClasses = inst.getAllLoadedClasses();
        SIO.info(id + " loadedClass.length=" + loadedClasses.length);
        for (Class c : loadedClasses) {
            SIO.info(id + " loadedClass: " + c.getCanonicalName());
        }

        ClassFileTransformer transformer = new InvokeTraceTransformer();

        inst.addTransformer(transformer, inst.isRetransformClassesSupported());

        start();

        SIO.info("premain 执行完成...");
    }

    /**
     * 打印参数示例
     */
    private static void printAbstract(Instrumentation inst) {
        SIO.info(
                "\n使用示例:\n 添加如下JVM参数:\n " +
                        "-Dgos.properties.path=${directory}/parameter.properties " +
                        "-javaagent:{directory}/gag-is-client.jar\n\n");
        String support = "支持";
        String unsupport = "不支持";
        SIO.info("是否支持类重新变型: " + (inst.isRetransformClassesSupported() ? support : unsupport));
        SIO.info("是否支持类重新定义:" + (inst.isRedefineClassesSupported() ? support : unsupport));
        SIO.info("是否支持本地方法前辍:" + (inst.isNativeMethodPrefixSupported() ? support : unsupport));
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
