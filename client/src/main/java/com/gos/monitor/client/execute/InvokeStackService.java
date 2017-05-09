package com.gos.monitor.client.execute;

import com.gos.monitor.client.entity.InvokeStack;
import com.gos.monitor.client.entity.InvokeTimer;
import com.gos.monitor.client.io.TimerWriter;
import com.gooagoo.monitor.common.MonitorSettings;
import com.gos.monitor.client.entity.InvokeStatisticsGroup;
import com.gooagoo.monitor.common.io.SIO;


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

        if (MonitorSettings.Client.IsStore) {
            TimerWriter.write(timer.toString());
        }

        InvokeStatisticsGroup.statistics(timer);

    }


    public static void finishNoneException(String methodName) {
        finish(methodName, false);
    }

    public static void finishHasException(String method) {
        finish(method, true);
    }
}
