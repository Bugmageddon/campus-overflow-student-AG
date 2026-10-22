package com.campusoverflow.reputation.infrastructure.leaderboard;

import com.campusoverflow.reputation.application.LeaderboardPeriod;
import com.campusoverflow.reputation.application.LeaderboardStore;
import com.campusoverflow.reputation.domain.AcademicTerm;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

/** 排行榜：每个周期一个 ZSET（lb:week:2026-W41 / lb:month:2026-10 / lb:term:2026-FALL），ZINCRBY 维护。 */
@Component
public class RedisLeaderboardStore implements LeaderboardStore {

    private final StringRedisTemplate redis;

    public RedisLeaderboardStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void increment(long userId, int delta, Instant at) {
        String member = String.valueOf(userId);
        incr("lb:week:" + AcademicTerm.weekKey(at), member, delta, Duration.ofDays(15));
        incr("lb:month:" + AcademicTerm.monthKey(at), member, delta, Duration.ofDays(40));
        incr("lb:term:" + AcademicTerm.termKey(at), member, delta, Duration.ofDays(200));
    }

    private void incr(String key, String member, int delta, Duration ttl) {
        redis.opsForZSet().incrementScore(key, member, delta);
        redis.expire(key, ttl);
    }

    @Override
    public List<Entry> top(LeaderboardPeriod period, Instant at, int limit) {
        String key = switch (period) {
            case WEEK -> "lb:week:" + AcademicTerm.weekKey(at);
            case MONTH -> "lb:month:" + AcademicTerm.monthKey(at);
            case TERM -> "lb:term:" + AcademicTerm.termKey(at);
            case ALL -> throw new IllegalArgumentException("总榜不由 Redis 提供");
        };
        Set<TypedTuple<String>> tuples = redis.opsForZSet().reverseRangeWithScores(key, 0, limit - 1L);
        if (tuples == null) {
            return List.of();
        }
        return tuples.stream()
                .filter(t -> t.getValue() != null && t.getScore() != null)
                .map(t -> new Entry(Long.parseLong(t.getValue()), Math.round(t.getScore())))
                .toList();
    }
}
