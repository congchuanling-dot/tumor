package io.github.tumor.annotation;

/**
 * Tumor 混沌注入器常量。
 */
public final class TumorConstants {

    /** 默认 CPU 饥饿比例 */
    public static final String DEFAULT_CPU = "50%";
    /** 默认持续时间 */
    public static final String DEFAULT_DURATION = "5s";
    /** 默认延迟 */
    public static final String DEFAULT_DELAY = "100ms";
    /** 默认内存泄漏单次大小 */
    public static final String DEFAULT_LEAK_SIZE = "10MB";
    /** 默认内存泄漏最大次数 */
    public static final int DEFAULT_LEAK_MAX = 50;
    /** 默认堆使用率阈值，超过则停止内存泄漏 */
    public static final double DEFAULT_HEAP_THRESHOLD = 0.8;

    private TumorConstants() {}
}
