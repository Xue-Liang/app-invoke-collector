package com.gos.monitor.client.transformer;

import com.gos.monitor.common.MonitorSettings;
import com.gos.monitor.common.Waiter;
import com.gos.monitor.common.io.SIO;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by xue on 2016-11-28.
 */
public class InvokeTraceTransformer implements ClassFileTransformer {
    private static final ConcurrentLinkedQueue<Runnable> WeavedClasssFiles = new ConcurrentLinkedQueue<>();
    private static volatile boolean ExitConsumer = false;
    private static final String WeavedClassesFileBasePath = MonitorSettings.Client.WeavedClassesFileBase;

    public InvokeTraceTransformer() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (!ExitConsumer) {
                    Runnable r = WeavedClasssFiles.poll();
                    if (r != null) {
                        r.run();
                        continue;
                    }

                    Waiter.waitFor(WeavedClasssFiles, 5000);
                }
            }
        };
        Thread consumer = new Thread(r, "WeavedClasssFiles-Conusmer");
        consumer.start();

        Runnable hook = new Runnable() {
            @Override
            public void run() {
                ExitConsumer = true;
                Waiter.notify(WeavedClasssFiles);
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(hook, "WeavedClassFiles-Hook"));
    }

    @Override
    public byte[] transform(ClassLoader loader, final String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        String cn = className.replaceAll("/", ".");
        try {
            SIO.info("step 1- 正在检查:" + cn);
            if (MonitorSettings.Client.ExcludePackages != null && MonitorSettings.Client.ExcludePackages.matcher(cn).find()) {
                SIO.info("因匹配排除表达式故跳过:" + cn);
                return null;
            } else if (MonitorSettings.Client.IncludePackages != null && !MonitorSettings.Client.IncludePackages.matcher(cn).find()) {
                SIO.info("因不匹配采集表达式故跳过:" + cn);
                return null;
            }
            if (classBeingRedefined != null) {
                if (classBeingRedefined.isInterface()) {
                    SIO.info("跳过接口:" + className.replace("/", "."));
                    return null;
                }
                if (classBeingRedefined.isAnnotation()) {
                    SIO.info("跳过注解:" + className.replace("/", "."));
                    return null;
                }
                if (classBeingRedefined.isArray()) {
                    SIO.info("跳过数组:" + className.replace("/", "."));
                    return null;
                }
                if (classBeingRedefined.isEnum()) {
                    SIO.info("跳过枚举:" + className.replace("/", "."));
                    return null;
                }
            }
            String filePath = className + ".class";
            SIO.info("tid:[" + Thread.currentThread().getId() + "]修改过的字节码将被保存在:" + WeavedClassesFileBasePath + filePath);

            ClassReader reader;
            byte[] bytes = getBytes(loader, filePath);
            if (bytes != null && bytes.length > 0) {
                reader = new ClassReader(bytes);
            } else {
                SIO.info("没有修:" + cn);
                return null;
            }
            ClassNode node = new ClassNode();
            reader.accept(node, 0);

            TryFinallyTransformService.transform(node);

            ClassWriter writer = new DirectClassWriter(loader, ClassWriter.COMPUTE_FRAMES);
            node.accept(writer);
            byte[] data = writer.toByteArray();
            write(className, data);
            return data;
        } catch (Throwable t) {
            SIO.error("tid:[" + Thread.currentThread().getId() + "]转换:" + cn + "时,发生异常.");
        }
        return null;
    }


    private static byte[] getBytes(final ClassLoader loader, final String path) {
        InputStream is = null;
        try {
            if (loader != null)
                is = loader.getSystemResourceAsStream(path);
            else
                is = ClassLoader.getSystemResourceAsStream(path);
        } catch (Exception e) {
            SIO.error("读取-1:" + path + "时发生异常.", e);
            return null;
        }

        if (is == null) {
            try {
                if (loader != null)
                    is = loader.getResourceAsStream(path);
            } catch (Exception e) {
                SIO.error("读取-2:" + path + "时发生异常.", e);
            }
        }

        if (is != null) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream(is.available())) {
                int size = 0;
                byte[] cup = new byte[1024];
                while ((size = is.read(cup)) > 0) {
                    bos.write(cup, 0, size);
                }
                return bos.toByteArray();
            } catch (IOException e) {
                SIO.error("读取-3:" + path + "时发生异常.", e);
            }
        }
        return null;
    }

    private static void write(final String path, final byte[] data) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                int ix = path.lastIndexOf((int) '/');
                String folder = path.substring(0, ix + 1);
                String name = folder.substring(ix + 1);
                String directory = WeavedClassesFileBasePath + folder;
                File dir = new File(directory);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String finalFilePath = directory + name + ".class";
                SIO.info("修改过的字节码已被保存在:" + finalFilePath);
                try (FileOutputStream fos = new FileOutputStream(finalFilePath)) {
                    fos.write(data);
                } catch (Exception e) {
                    SIO.error("输出字节码文件出错.", e);
                }
            }
        };
        WeavedClasssFiles.add(r);
    }
}