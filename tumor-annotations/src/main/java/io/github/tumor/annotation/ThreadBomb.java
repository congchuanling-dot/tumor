package io.github.tumor.annotation;

import java.lang.annotation.*;

/**
 * 癌细胞疯狂分裂（线程爆炸）。
 * 瞬间创建大量线程并保持活跃一段时间。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ThreadBomb {
    /** 创建的线程数量 */
    int count() default 200;
    /** 保持时长，支持 "10s", "5m" */
    String duration() default "10s";
    /** 触发概率 0.0 ~ 1.0 */
    double probability() default 1.0;
}
