package com.gos.monitor.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用于描述被监控的接口方法.
 * Created by Xue Liang on 2017-05-11.
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface Mark {

    /**
     * 方法的可理解的名称.
     * <p>
     * 如:支付宝支付
     *
     * @return
     */
    String name() default "";

    /**
     * 方法功能的简述
     *
     * @return
     */
    String description() default "";

    /**
     * 允许方法的平均耗时最长时间(单位:毫秒)
     * 平均每次调用耗时超过此值，发出警报.
     *
     * @return
     */
    int maxAverageTime() default 1000;


    /**
     * 　在一段时间内最多允许出现的错误数.
     * 　超过此值,发出警报.
     */
    int maxError() default 0;

}
