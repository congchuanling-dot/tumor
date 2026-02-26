package io.github.tumor.annotation;

import java.lang.annotation.*;

/**
 * 癌细胞突变（异常抛出）。
 * 按概率抛出指定类型的异常。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceptionInject {
    /** 要抛出的异常类型 */
    Class<? extends Throwable>[] types() default {RuntimeException.class};
    /** 抛出概率 0.0 ~ 1.0 */
    double rate() default 0.3;
}
