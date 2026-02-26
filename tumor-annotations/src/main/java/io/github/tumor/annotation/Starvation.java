package io.github.tumor.annotation;

import java.lang.annotation.*;

/**
 * 癌细胞抢夺营养（CPU 饥饿）。
 * 后台启动死循环线程抢占指定比例 CPU，持续一段时间。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Starvation {
    /** CPU 占用比例，支持 "30%", "0.3" */
    String cpu() default "50%";
    /** 持续时间，支持 "3000ms", "5s", "1m" */
    String duration() default "5s";
    /** 触发概率 0.0 ~ 1.0 */
    double probability() default 1.0;
}
