package io.github.tumor.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 注解参数解析工具：百分比、时长、大小、延迟区间。
 */
public final class ParseUtils {

    private static final Pattern PERCENT = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*%?$");
    private static final Pattern DURATION_MS = Pattern.compile("^(\\d+)\\s*ms$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DURATION_S = Pattern.compile("^(\\d+)\\s*s$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DURATION_M = Pattern.compile("^(\\d+)\\s*m$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SIZE_BYTES = Pattern.compile("^(\\d+)\\s*B$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SIZE_KB = Pattern.compile("^(\\d+)\\s*KB$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SIZE_MB = Pattern.compile("^(\\d+)\\s*MB$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SIZE_GB = Pattern.compile("^(\\d+)\\s*GB$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DELAY_RANGE = Pattern.compile("^(\\d+)\\s*ms\\s*-\\s*(\\d+)\\s*ms$", Pattern.CASE_INSENSITIVE);

    /**
     * 解析百分比，如 "30%", "0.3" -> 0.3
     */
    public static double parsePercent(String value) {
        if (value == null || value.isBlank()) return 0.5;
        value = value.trim();
        Matcher m = PERCENT.matcher(value);
        if (m.matches()) {
            double v = Double.parseDouble(m.group(1));
            return value.contains("%") ? v / 100.0 : Math.min(1.0, Math.max(0, v));
        }
        return 0.5;
    }

    /**
     * 解析持续时间，支持 "3000ms", "5s", "1m" -> 毫秒
     */
    public static long parseDuration(String value) {
        if (value == null || value.isBlank()) return 5000L;
        value = value.trim();
        Matcher ms = DURATION_MS.matcher(value);
        if (ms.matches()) return Long.parseLong(ms.group(1));
        Matcher s = DURATION_S.matcher(value);
        if (s.matches()) return Long.parseLong(s.group(1)) * 1000L;
        Matcher m = DURATION_M.matcher(value);
        if (m.matches()) return Long.parseLong(m.group(1)) * 60_000L;
        return 5000L;
    }

    /**
     * 解析大小，支持 "10MB", "1KB" -> 字节数
     */
    public static int parseSize(String value) {
        if (value == null || value.isBlank()) return 10 * 1024 * 1024;
        value = value.trim().toUpperCase();
        Matcher gb = SIZE_GB.matcher(value);
        if (gb.matches()) return (int) (Long.parseLong(gb.group(1)) * 1024L * 1024 * 1024);
        Matcher mb = SIZE_MB.matcher(value);
        if (mb.matches()) return (int) (Long.parseLong(mb.group(1)) * 1024L * 1024);
        Matcher kb = SIZE_KB.matcher(value);
        if (kb.matches()) return (int) (Long.parseLong(kb.group(1)) * 1024L);
        Matcher b = SIZE_BYTES.matcher(value);
        if (b.matches()) return Integer.parseInt(b.group(1));
        return 10 * 1024 * 1024;
    }

    /**
     * 解析延迟：若为 "200ms-800ms" 返回 [minMs, maxMs]；否则解析为固定 ms，min=max。
     */
    public static long[] parseDelayRange(String value) {
        if (value == null || value.isBlank()) return new long[]{100L, 100L};
        value = value.trim();
        Matcher range = DELAY_RANGE.matcher(value);
        if (range.matches()) {
            return new long[]{Long.parseLong(range.group(1)), Long.parseLong(range.group(2))};
        }
        long ms = parseDuration(value);
        return new long[]{ms, ms};
    }

    private ParseUtils() {}
}
