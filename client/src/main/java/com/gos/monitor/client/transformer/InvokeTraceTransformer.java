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
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by xue on 2016-11-28.
 */
public class InvokeTraceTransformer implements ClassFileTransformer {
    private static final ConcurrentLinkedQueue<Runnable> WeavedClasssFileQueue = new ConcurrentLinkedQueue<>();
    private static volatile boolean ExitConsumer = false;
    private static final String WeavedClassesFileBasePath = MonitorSettings.Client.WeavedClassesFileBase;

    static {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (!ExitConsumer) {
                    Runnable r = WeavedClasssFileQueue.poll();
                    if (r != null) {
                        r.run();
                        continue;
                    }

                    Waiter.waitFor(WeavedClasssFileQueue, 5000);
                }
            }
        };
        Thread consumer = new Thread(r, "WeavedClasssFiles-Conusmer");
        consumer.start();

        Runnable hook = new Runnable() {
            @Override
            public void run() {
                ExitConsumer = true;
                Waiter.notify(WeavedClasssFileQueue);
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(hook, "WeavedClassFiles-Hook"));
    }

    public InvokeTraceTransformer() {

    }

    @Override
    public byte[] transform(ClassLoader loader, final String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] buff) throws IllegalClassFormatException {
        String cn = className.replaceAll("/", ".");

        try {
            SIO.info("step 1- 正在检查:" + cn);
            SIO.info("step 1.1- 类:" + cn + "　的大小:" + (buff == null ? "null" : Integer.toString(buff.length)));
            if (MonitorSettings.Client.ExcludePackages() != null && MonitorSettings.Client.ExcludePackages().matcher(cn).find()) {
                SIO.info("因匹配排除表达式故跳过:" + cn);
                return null;
            } else if (MonitorSettings.Client.IncludePackages() != null && !MonitorSettings.Client.IncludePackages().matcher(cn).find()) {
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

            final String filePath = className + ".class";

            ClassReader reader;
            byte[] bytes = buff != null ? buff : getBytes(loader, filePath);
            if (bytes != null && bytes.length > 0) {
                reader = new ClassReader(bytes);
            } else {
                SIO.info("因没有读取到字节码数据,所以没有修改:" + cn);
                return null;
            }
            ClassNode node = new ClassNode();
            reader.accept(node, 0);

            boolean hasWeaved = TryFinallyTransformService.transform(node);
            if (!hasWeaved) {
                SIO.info("因所有方法没有满足修改条件,所以没有修改:" + cn);
                return null;
            }

            ClassWriter writer = new DirectClassWriter(loader, ClassWriter.COMPUTE_FRAMES);
            node.accept(writer);
            byte[] data = writer.toByteArray();

            Runnable r = new WriteClassFileTask(className, data);
            WeavedClasssFileQueue.add(r);
            return data;
        } catch (Throwable t) {
            SIO.error("tid:[" + Thread.currentThread().getId() + "]转换:" + cn + "时,发生异常.");
        }
        return null;
    }

    private static byte[] getBytes(final ClassLoader loader, final String path) {
        if (path == null || path.length() < 1) {
            return null;
        }
        InputStream is = loader != null ? loader.getSystemResourceAsStream(path) : ClassLoader.getSystemResourceAsStream(path);
        if (is == null) {
            SIO.warn("读取-1:" + path + ",没读取到字节码数据.");
            return null;
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(is.available())) {
            byte[] cup = new byte[1024];
            for (int size; (size = is.read(cup)) > 0; ) {
                bos.write(cup, 0, size);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            SIO.error("读取-3:" + path + "时发生异常.", e);
        }
        return null;
    }


    static class WriteClassFileTask implements Runnable {
        private String path;
        private byte[] data;

        WriteClassFileTask(String path, byte[] data) {
            this.path = path;
            this.data = data;
        }

        @Override
        public void run() {
            int ix = path.lastIndexOf((int) '/');
            String folder = this.path.substring(0, ix + 1);
            String name = this.path.substring(ix + 1);
            String directory = WeavedClassesFileBasePath + folder;
            File dir = new File(directory);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    return;
                }
            }
            String finalFilePath = directory + name + ".class";
            SIO.info("修改过的字节码已被保存在:" + finalFilePath);
            try (FileOutputStream fos = new FileOutputStream(finalFilePath)) {
                fos.write(data);
            } catch (Exception e) {
                SIO.error("输出字节码文件出错.", e);
            }
        }

    }
}