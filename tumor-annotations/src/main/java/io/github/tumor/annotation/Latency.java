package io.github.tumor.annotation;

import java.lang.annotation.*;

/**
 * 癌细胞阻塞通道（延迟）。
 * 方法执行前后 sleep 随机或固定时间。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Latency {
    /** 延迟时间，支持 "100ms", "200ms-800ms"（随机区间） */
    String delay() default "100ms";
    /** 触发概率 0.0 ~ 1.0 */
    double probability() default 1.0;
}
