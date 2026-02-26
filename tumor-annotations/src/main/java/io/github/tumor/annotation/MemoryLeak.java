package io.github.tumor.annotation;

import java.lang.annotation.*;

/**
 * 癌细胞无限增殖（内存泄漏）。
 * 每次调用分配大 byte[] 放入静态 List 不释放，直到接近上限自动停止。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MemoryLeak {
    /** 每次泄漏大小，支持 "10MB", "1MB" */
    String size() default "10MB";
    /** 最大泄漏次数，超过后不再添加 */
    int max() default 50;
    /** 触发概率 0.0 ~ 1.0 */
    double probability() default 0.5;
}
