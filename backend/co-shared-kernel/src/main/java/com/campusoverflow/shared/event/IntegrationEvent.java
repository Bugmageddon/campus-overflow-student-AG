package com.campusoverflow.shared.event;

import java.time.Instant;

/**
 * 跨限界上下文传播的集成事件（发布语言）。
 * <ul>
 *   <li>必须是不可变的 record，只包含基本类型与字符串，避免泄露领域模型；</li>
 *   <li>{@code eventId} 全局唯一，消费者据此实现幂等（至少一次投递语义）。</li>
 * </ul>
 */
public interface IntegrationEvent {
    String eventId();

    Instant occurredAt();
}
