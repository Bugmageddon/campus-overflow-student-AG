package com.campusoverflow.shared.event;

/**
 * 消费端幂等守卫。在处理器事务内调用：首次处理返回 true；重复投递返回 false，处理器应直接跳过。
 */
public interface ProcessedEventStore {
    boolean markProcessed(String eventId, String consumer);
}
