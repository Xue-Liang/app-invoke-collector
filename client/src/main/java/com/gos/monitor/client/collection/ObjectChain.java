package com.gos.monitor.client.collection;

import java.io.Serializable;

/**
 * 一个简单的单向链表
 *
 * @param <T> 链表的结点数据
 */
public class ObjectChain<T> implements Serializable {

    private int size = 0;

    private ObjectNode<T> first;

    public void push(T obj) {
        ObjectNode<T> node = new ObjectNode(obj);
        if (first == null) {
            this.first = node;
        } else {
            node.next = this.first;
            this.first = node;
        }
        this.size++;
    }

    public T pop() {
        if (this.first == null) {
            return null;
        }
        ObjectNode<T> on = this.first;
        this.first = this.first.next;
        this.size--;
        return on.data;
    }

    public int size() {
        return this.size;
    }

    public boolean hasMore() {
        return this.size > 0;
    }

    public void clear() {
        this.first = null;
        this.size = 0;
    }

    /**
     * 链表结点
     *
     * @param <T> 结点数据类型
     */

    private class ObjectNode<T> implements Serializable {
        /**
         * 数据域
         */
        private T data;

        /**
         * 下一个结点
         */
        private ObjectNode<T> next = null;

        public ObjectNode(T obj) {
            this.data = obj;
        }

        public T getData() {
            return this.data;
        }

        public ObjectNode<T> next() {
            return this.next;
        }
    }
}
