package io.github.tumor.core;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * 运行时信息：堆使用率等，用于内存泄漏模块自动停止。
 */
public final class TumorRuntime {

    private static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();

    /**
     * 当前堆使用率 0.0 ~ 1.0。
     */
    public static double heapUsageRatio() {
        MemoryUsage heap = MEMORY_MX_BEAN.getHeapMemoryUsage();
        long max = heap.getMax() > 0 ? heap.getMax() : heap.getCommitted();
        if (max <= 0) return 0;
        return (double) heap.getUsed() / max;
    }

    /**
     * 是否超过给定阈值（默认 0.8 建议停止泄漏）。
     */
    public static boolean isHeapAboveThreshold(double threshold) {
        return heapUsageRatio() >= threshold;
    }

    private TumorRuntime() {}
}
