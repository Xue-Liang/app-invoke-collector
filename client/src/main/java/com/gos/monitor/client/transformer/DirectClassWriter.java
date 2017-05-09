package com.gos.monitor.client.transformer;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * 重写了ClassWriter的 getCommonSuperClass 方法.
 * 在指定的类加载器查找类.避免产生 ClassNotFoundException
 * Created by xue on 2017-03-07.
 */
public class DirectClassWriter extends ClassWriter {
    private ClassLoader loader;

    public DirectClassWriter(int flags) {
        super(flags);
    }

    public DirectClassWriter(ClassReader classReader, int flags) {
        super(classReader, flags);
    }

    public DirectClassWriter(ClassLoader loader, int flags) {
        super(flags);
        this.loader = loader;
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        Class<?> c, d;
        try {
            if (this.loader == null) {
                this.loader = getClass().getClassLoader();
            }
            c = Class.forName(type1.replace('/', '.'), false, this.loader);
            d = Class.forName(type2.replace('/', '.'), false, this.loader);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
        if (c.isAssignableFrom(d)) {
            return type1;
        }
        if (d.isAssignableFrom(c)) {
            return type2;
        }
        if (c.isInterface() || d.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                c = c.getSuperclass();
            } while (!c.isAssignableFrom(d));
            return c.getName().replace('.', '/');
        }
    }

}
