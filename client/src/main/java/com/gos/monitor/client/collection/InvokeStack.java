package com.gos.monitor.client.collection;


import com.gos.monitor.client.entity.InvokeTimer;

import java.util.LinkedList;
import java.util.UUID;

/**
 * 一个线程中方法调用栈,方法开始执行时，压栈，方法结束返回时出栈
 *
 * @author Xue Liang on 2017-03-01
 */
public class InvokeStack {

    private long step = 1L;

    private String id;

    private ObjectChain<InvokeTimer> stack = new ObjectChain<>();

    public InvokeStack() {
        this.id = UUID.randomUUID().toString();
    }

    public void push(InvokeTimer timer) {
        timer.setStep(this.step);
        this.stack.push(timer);
        this.step++;
    }

    public InvokeTimer pop() {
        InvokeTimer timer = null;
        if (stack.size() > 0) {
            timer = stack.pop();
        }
        return timer;
    }

    public String getId() {
        return this.id;
    }

    public int size() {
        return this.stack.size();
    }

    public void clear() {
        this.id = UUID.randomUUID().toString();
        this.step = 1L;
        this.stack.clear();
    }
}
