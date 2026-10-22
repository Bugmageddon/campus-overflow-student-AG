package com.campusoverflow.bootstrap.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 统一时钟：业务代码注入 Clock 而非直接调用 Instant.now()，测试时可替换为固定时钟。 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
