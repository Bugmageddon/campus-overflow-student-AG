package com.campusoverflow.qa.infrastructure.cache;

import com.campusoverflow.qa.application.port.QuestionListCache;
import com.campusoverflow.qa.application.query.QuestionSummaryView;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Cache-Aside 列表缓存。失效采用“代际号”：写操作把 qa:list:gen 加 1，旧 key 自然过期，无需 KEYS 扫描。
 * 失效动作延迟到事务提交之后执行，避免并发读把提交前的旧数据重新写回缓存。
 */
@Component
public class RedisQuestionListCache implements QuestionListCache {

    private static final Logger log = LoggerFactory.getLogger(RedisQuestionListCache.class);
    private static final String GEN_KEY = "qa:list:gen";
    private static final TypeReference<PageResult<QuestionSummaryView>> TYPE = new TypeReference<>() {
    };

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisQuestionListCache(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public Optional<PageResult<QuestionSummaryView>> get(Long courseId, PageRequest page) {
        try {
            String json = redis.opsForValue().get(key(courseId, page));
            return json == null ? Optional.empty() : Optional.of(mapper.readValue(json, TYPE));
        } catch (Exception e) {
            log.debug("列表缓存读取失败，回源数据库: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(Long courseId, PageRequest page, PageResult<QuestionSummaryView> value) {
        try {
            Duration ttl = Duration.ofSeconds(60 + ThreadLocalRandom.current().nextInt(15)); // TTL 抖动防雪崩
            redis.opsForValue().set(key(courseId, page), mapper.writeValueAsString(value), ttl);
        } catch (Exception e) {
            log.debug("列表缓存写入失败: {}", e.getMessage());
        }
    }

    @Override
    public void invalidateAll() {
        Runnable bump = () -> {
            try {
                redis.opsForValue().increment(GEN_KEY);
            } catch (RuntimeException e) {
                log.warn("列表缓存失效失败（最多 75 秒后自然过期）: {}", e.getMessage());
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    bump.run();
                }
            });
        } else {
            bump.run();
        }
    }

    private String key(Long courseId, PageRequest page) {
        String gen = Optional.ofNullable(redis.opsForValue().get(GEN_KEY)).orElse("0");
        return "qa:list:" + gen + ":" + (courseId == null ? "all" : courseId) + ":" + page.page() + ":" + page.size();
    }
}
