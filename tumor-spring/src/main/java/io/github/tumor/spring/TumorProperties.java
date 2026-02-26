package io.github.tumor.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tumor 配置属性。
 * <ul>
 *   <li>tumor.enabled=true 时且 profile 匹配时才启用注入</li>
 *   <li>生产环境建议 enabled=false 或 profile 仅包含 test/staging</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "tumor")
public class TumorProperties {

    /** 是否启用混沌注入，默认 false（生产安全） */
    private boolean enabled = false;

    /**
     * 仅在以下 profile 下生效；为空表示不按 profile 限制。
     * 配置示例: tumor.profile-only=test,staging,dev 或 YAML 列表
     */
    private String profileOnly = "";

    /** 堆使用率超过此比例时停止内存泄漏，默认 0.8 */
    private double heapThreshold = 0.8;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProfileOnly() {
        return profileOnly;
    }

    public void setProfileOnly(String profileOnly) {
        this.profileOnly = profileOnly != null ? profileOnly : "";
    }

    /** 解析为 profile 集合，便于切面判断 */
    public Set<String> getProfileOnlySet() {
        if (profileOnly == null || profileOnly.isBlank()) return Collections.emptySet();
        return Arrays.stream(profileOnly.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public double getHeapThreshold() {
        return heapThreshold;
    }

    public void setHeapThreshold(double heapThreshold) {
        this.heapThreshold = heapThreshold;
    }
}
