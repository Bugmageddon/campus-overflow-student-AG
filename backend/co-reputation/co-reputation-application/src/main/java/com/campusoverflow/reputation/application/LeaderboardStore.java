package com.campusoverflow.reputation.application;

import java.time.Instant;
import java.util.List;

/** 排行榜存储端口（实现：Redis ZSET，ADR 讨论见 8.x 缓存章节）。 */
public interface LeaderboardStore {

    record Entry(long userId, long score) {
    }

    void increment(long userId, int delta, Instant at);

    List<Entry> top(LeaderboardPeriod period, Instant at, int limit);
}
