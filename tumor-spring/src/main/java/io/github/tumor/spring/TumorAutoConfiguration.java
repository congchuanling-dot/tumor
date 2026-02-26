package io.github.tumor.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

/**
 * Tumor 自动配置。
 * 仅当 tumor.enabled=true 时生效，默认不启用以保证生产安全。
 * TumorProperties 由 @EnableConfigurationProperties 注册，勿再手写 @Bean 避免重复。
 */
@Configuration
@ConditionalOnProperty(prefix = "tumor", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableAspectJAutoProxy
@EnableConfigurationProperties(TumorProperties.class)
public class TumorAutoConfiguration {

    @Bean
    public TumorAspect tumorAspect(TumorProperties tumorProperties, Environment environment) {
        return new TumorAspect(tumorProperties, environment);
    }
}
