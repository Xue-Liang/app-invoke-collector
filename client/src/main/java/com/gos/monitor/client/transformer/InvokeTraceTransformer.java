package com.gos.monitor.client.transformer;

import com.gooagoo.monitor.common.MonitorSettings;
import com.gooagoo.monitor.common.io.SIO;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by xue on 2016-11-28.
 */
public class InvokeTraceTransformer implements ClassFileTransformer {

    private static String WeavedClassesFilePath = MonitorSettings.Client.WeavedClassesFileBase;

    public InvokeTraceTransformer() {

    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        String cn = className.replaceAll("/", ".");
        byte[] data = null;
        try {
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

            if (cn != null) {
                if (MonitorSettings.Client.ExcludePackages.matcher(cn).find()) {
                    SIO.info("因匹配排除表达式故跳过:" + cn);
                    return null;
                } else if (!MonitorSettings.Client.IncludePackages.matcher(cn).find()) {
                    SIO.info("因不匹配采集表达式故跳过:" + cn);
                    return null;
                }
            }

            SIO.info("step 1- 将要修改:" + cn);
            String filePath = className + ".class";
            SIO.info("tid:[" + Thread.currentThread().getId() + "]修改过的字节码将被保存在:" + WeavedClassesFilePath + filePath);

            ClassReader reader;
            byte[] bytes = this.getBytes(loader, filePath);
            if (bytes != null && bytes.length > 0) {
                reader = new ClassReader(bytes);
            } else {
                SIO.info("无法修改类:" + cn + " 因为找不到...");
                return null;
            }
            ClassNode node = new ClassNode();
            reader.accept(node, 0);

            TryFinallyTransformService transformService = new TryFinallyTransformService(className);
            transformService.transform(node);

            ClassWriter writer = new DirectClassWriter(loader, ClassWriter.COMPUTE_FRAMES);
            node.accept(writer);

            data = writer.toByteArray();

            int ix = className.lastIndexOf((int) '/');
            String packageName = className.substring(0, ix + 1);
            String classFileName = className.substring(ix + 1);
            String classFileDirectory = WeavedClassesFilePath + packageName;
            File dir = new File(classFileDirectory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String finalFilePath = classFileDirectory + classFileName + ".class";
            SIO.info("tid:[" + Thread.currentThread().getId() + "]修改过的字节码已被保存在:" + finalFilePath);
            try (FileOutputStream fos = new FileOutputStream(finalFilePath)) {
                fos.write(data);
            } catch (Exception e) {
                SIO.error("输出字节码文件出错.", e);
            }
        } catch (Throwable t) {
            SIO.error("tid:[" + Thread.currentThread().getId() + "]转换:" + cn + "时,发生异常.");
        }
        return data;
    }


    private byte[] getBytes(ClassLoader loader, String path) {
        InputStream is = null;
        try {
            if (loader != null)
                is = loader.getSystemResourceAsStream(path);
            else
                is = ClassLoader.getSystemResourceAsStream(path);
        } catch (Exception e) {
        }

        if (is == null) {
            try {
                if (loader != null)
                    is = loader.getResourceAsStream(path);
            } catch (Exception e) {
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

            }
        }
        return null;
    }
}