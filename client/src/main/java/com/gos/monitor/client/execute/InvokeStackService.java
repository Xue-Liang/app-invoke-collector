package com.gos.monitor.client.execute;

import com.gos.monitor.annotation.RequireCare;
import com.gos.monitor.client.collection.InvokeStack;
import com.gos.monitor.client.collection.InvokeStatisticsBucket;
import com.gos.monitor.client.entity.InvokeTimer;
import com.gos.monitor.client.mapping.RequireCareMapping;
import com.gos.monitor.common.MonitorSettings;
import com.gos.monitor.common.io.SIO;
import sun.misc.Request;

import java.lang.reflect.Method;


/**
 * 在给业务类织入代码时，使用的就是本类提供的 start 和  finish 方法
 *
 * @see #start(String methodName)
 * @see #finishNoneException(String)
 * @see #finishHasException(String)
 * <p>
 * Created by xue on 16-11-23.
 */
public class InvokeStackService {

    private static final ThreadLocal<InvokeStack> SyncInvokeInstance = new ThreadLocal<>();

    private InvokeStackService() {

    }

    private static void cleanup() {
        InvokeStack stack = SyncInvokeInstance.get();
        if (stack != null) {
            stack.clear();
        }
    }

    public static void start(final String methodName) {
        if (methodName == null) {
            return;
        }
        //---
        InvokeStack stack = SyncInvokeInstance.get();
        if (stack == null) {
            stack = new InvokeStack();
            SyncInvokeInstance.set(stack);
        }
        InvokeTimer timer = new InvokeTimer(stack.getId(), methodName);
        stack.push(timer);

        if (MonitorSettings.Client.Logging) {
            SIO.info(" 第[" + Long.toString(timer.getStep()) + "]步开始 执行序列号:" + stack.getId() + " 方法:" + methodName);
        }
    }

    public static void finish(final String methodName, boolean hasException) {
        if (methodName == null) {
            return;
        }
        //得到当前调用栈
        final InvokeStack stack = SyncInvokeInstance.get();
        if (stack == null) {
            return;
        }
        //step 1.从[调用轨迹栈]中弹出方法计时器
        //---
        InvokeTimer timer = stack.pop();
        if (timer == null) {
            return;
        }
        if (methodName.equals(timer.getMethodName())) {
            timer.finish();
        }
        //---
        if (MonitorSettings.Client.Logging) {
            SIO.info(" 第[" + Long.toString(timer.getStep()) + "]步完成 执行序列号:" + stack.getId() + " 方法:" + methodName + " -耗时约:" + (timer.getElapsed() / 1_000_000) + " ms(" + timer.getElapsed() + " ns).");
        }

        if (stack.size() < 1) {
            stack.clear();
        }

        timer.setHasException(hasException);


        mapping(methodName);

        InvokeStatisticsBucket.statistics(timer);

    }


    public static void finishNoneException(String methodName) {
        finish(methodName, false);
    }

    public static void finishHasException(String method) {
        finish(method, true);
    }

    /**
     * 将方法和RequireCare注解关联起来.
     * 方便根据方法全名,得以注解信息.
     *
     * @param mn 方法全名
     */
    private static void mapping(String mn) {
        //如果已经发生了映射，跳过.
        if (RequireCareMapping.hasKey(mn)) {
            return;
        }
        //如果此类已经通过反射进行了方法映射则跳过,不做反射处理.
        String clazz = getClassName(mn);
        if (RequireCareMapping.hasClass(clazz)) {
            return;
        }

        RequireCareMapping.setClass(clazz);

        try {
            Class<?> cs = Class.forName(clazz);
            Method[] ms = cs.getDeclaredMethods();
            for (Method m : ms) {
                RequireCare annotation = m.getAnnotation(RequireCare.class);
                String methodFullName = getFullName(m);
                if (!RequireCareMapping.hasKey(methodFullName)) {
                    RequireCareMapping.put(methodFullName, annotation);
                }
            }
        } catch (Exception e) {
            SIO.error("反射:" + clazz + "时发生异常.", e);
        }
    }

    private static String getFullName(Method m) {
        StringBuilder fullName = new StringBuilder(128);
        fullName.append(m.getDeclaringClass().getName())
                .append(".").append(m.getName())
                .append("(");
        Class<?>[] cs = m.getParameterTypes();
        int i = 0, len = cs.length - 1;
        for (; i < len; i++) {
            fullName.append(cs[i].getSimpleName()).append(",");
        }
        if (i < cs.length) {
            fullName.append(cs[i].getSimpleName());
        }
        fullName.append(")");
        return fullName.toString();
    }

    private static String getClassName(String methodFullName) {
        if (methodFullName == null) {
            return null;
        }
        int ix = methodFullName.lastIndexOf('.');
        return ix < 1 ? null : methodFullName.substring(0, ix);
    }
}
