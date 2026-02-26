package io.github.tumor.spring;

import io.github.tumor.annotation.*;
import io.github.tumor.core.ParseUtils;
import io.github.tumor.core.TumorRuntime;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Tumor 混沌注入切面。
 * 根据注解在方法执行前后注入 CPU 饥饿、延迟、内存泄漏、异常、线程爆炸等故障。
 */
@Aspect
public class TumorAspect {

    private static final Logger log = LoggerFactory.getLogger(TumorAspect.class);

    /** 内存泄漏桶：持有分配的 byte[] 不释放，达 max 或堆超阈值后停止 */
    private static final List<byte[]> LEAK_BUCKET = Collections.synchronizedList(new ArrayList<>());

    private final TumorProperties props;
    private final Environment environment;

    public TumorAspect(TumorProperties props) {
        this.props = props;
        this.environment = null;
    }

    public TumorAspect(TumorProperties props, Environment environment) {
        this.props = props;
        this.environment = environment;
    }

    private boolean isEnabled() {
        if (!props.isEnabled()) return false;
        Set<String> profileOnly = props.getProfileOnlySet();
        if (profileOnly.isEmpty()) return true;
        if (environment == null) return true;
        String[] actives = environment.getActiveProfiles();
        for (String p : actives) {
            if (profileOnly.contains(p)) return true;
        }
        return false;
    }

    private boolean shouldInject(double probability) {
        return isEnabled() && Math.random() <= probability;
    }

    // ---------- @Starvation ----------
    @Around("@annotation(starvation)")
    public Object aroundStarvation(ProceedingJoinPoint jp, Starvation starvation) throws Throwable {
        if (!shouldInject(starvation.probability())) return jp.proceed();

        double rate = ParseUtils.parsePercent(starvation.cpu());
        long ms = ParseUtils.parseDuration(starvation.duration());
        int threads = Math.max(1, (int) (rate * Runtime.getRuntime().availableProcessors() * 1.2));
        ExecutorService executor = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r, "tumor-starvation-" + r.hashCode());
            t.setDaemon(true);
            return t;
        });
        CountDownLatch latch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    long end = System.currentTimeMillis() + ms;
                    while (System.currentTimeMillis() < end) {
                        // 紧忙循环消耗 CPU
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            return jp.proceed();
        } finally {
            executor.shutdownNow();
            try { latch.await(ms + 1000, TimeUnit.MILLISECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            log.info("Starvation injected: {}% for {}ms on {}", rate * 100, ms, jp.getSignature());
        }
    }

    // ---------- @Latency ----------
    @Around("@annotation(latency)")
    public Object aroundLatency(ProceedingJoinPoint jp, Latency latency) throws Throwable {
        if (!shouldInject(latency.probability())) return jp.proceed();

        long[] range = ParseUtils.parseDelayRange(latency.delay());
        long delayMs = range[0] == range[1] ? range[0] : range[0] + (long) (Math.random() * (range[1] - range[0]));
        Thread.sleep(delayMs);
        try {
            return jp.proceed();
        } finally {
            log.debug("Latency injected: {}ms on {}", delayMs, jp.getSignature());
        }
    }

    // ---------- @MemoryLeak ----------
    @Around("@annotation(leak)")
    public Object aroundMemoryLeak(ProceedingJoinPoint jp, MemoryLeak leak) throws Throwable {
        if (!shouldInject(leak.probability())) return jp.proceed();
        if (TumorRuntime.isHeapAboveThreshold(props.getHeapThreshold())) {
            log.warn("Heap above threshold ({}), skip memory leak", props.getHeapThreshold());
            return jp.proceed();
        }
        if (LEAK_BUCKET.size() >= leak.max()) {
            log.warn("Memory leak bucket reached max ({}), stopping", leak.max());
            return jp.proceed();
        }

        int bytes = ParseUtils.parseSize(leak.size());
        byte[] chunk = new byte[bytes];
        Arrays.fill(chunk, (byte) 0xFF);
        LEAK_BUCKET.add(chunk);

        return jp.proceed();
    }

    // ---------- @ExceptionInject ----------
    @Around("@annotation(exceptionInject)")
    public Object aroundException(ProceedingJoinPoint jp, ExceptionInject exceptionInject) throws Throwable {
        if (!isEnabled() || Math.random() > exceptionInject.rate()) return jp.proceed();

        Class<? extends Throwable>[] types = exceptionInject.types();
        if (types == null || types.length == 0) return jp.proceed();
        Class<? extends Throwable> type = types[ThreadLocalRandom.current().nextInt(types.length)];
        try {
            Throwable t = type.getDeclaredConstructor(String.class).newInstance("Tumor injected: " + type.getSimpleName());
            throw t;
        } catch (ReflectiveOperationException e) {
            try {
                Throwable t = type.getDeclaredConstructor().newInstance();
                throw t;
            } catch (ReflectiveOperationException e2) {
                throw new RuntimeException("Tumor injected: " + type.getSimpleName(), e2);
            }
        }
    }

    // ---------- @ThreadBomb ----------
    @Around("@annotation(threadBomb)")
    public Object aroundThreadBomb(ProceedingJoinPoint jp, ThreadBomb threadBomb) throws Throwable {
        if (!shouldInject(threadBomb.probability())) return jp.proceed();

        int count = threadBomb.count();
        long durationMs = ParseUtils.parseDuration(threadBomb.duration());
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            Thread t = new Thread(() -> {
                try {
                    latch.countDown();
                    Thread.sleep(durationMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "tumor-threadbomb-" + i);
            t.setDaemon(true);
            t.start();
        }
        try {
            return jp.proceed();
        } finally {
            log.info("ThreadBomb injected: {} threads for {}ms on {}", count, durationMs, jp.getSignature());
        }
    }
}
