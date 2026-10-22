package com.campusoverflow.identity.infrastructure.security;

import com.campusoverflow.identity.application.LoginThrottle;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 基于 Redis 固定窗口计数的登录限流：同一账号 1 分钟内失败 N 次即锁定到窗口结束。 */
@Component
public class RedisLoginThrottle implements LoginThrottle {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final StringRedisTemplate redis;
    private final int maxFailures;

    public RedisLoginThrottle(StringRedisTemplate redis, @Value("${co.security.login-max-failures:5}") int maxFailures) {
        this.redis = redis;
        this.maxFailures = maxFailures;
    }

    @Override
    public boolean isBlocked(String username) {
        String v = redis.opsForValue().get(key(username));
        return v != null && Integer.parseInt(v) >= maxFailures;
    }

    @Override
    public void recordFailure(String username) {
        Long count = redis.opsForValue().increment(key(username));
        if (count != null && count == 1L) {
            redis.expire(key(username), WINDOW);
        }
    }

    @Override
    public void reset(String username) {
        redis.delete(key(username));
    }

    private static String key(String username) {
        return "id:login-fail:" + username;
    }
}
