package com.gos.monitor.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用于描述被监控的方法的注解.
 * 此注解一定要加到实现类的方法上.
 * Created by Xue Liang on 2017-05-11.
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface RequireCare {

    /**
     * 方法的可理解的名称.
     * <p>
     * 如:支付宝支付
     *
     * @return
     */
    String name() default "";

    /**
     * 接口的重要程度.
     *
     * @return
     */
    Level level() default Level.Normal;

    /**
     * 方法功能的简述
     *
     * @return
     */
    String description() default "";

    /**
     * 允许方法的平均耗时最长时间(单位:毫秒)
     * 平均每次调用耗时超过此值，发出警报.
     * 如果此值小于等于0,则不报警.
     *
     * @return
     */
    int maxAverageTime() default 1000;


    /**
     * 　在一段时间内最多允许出现的错误数.
     * 　超过此值,发出警报.
     *  如果此值小于等于零则不报警.
     */
    int maxError() default 0;

}
