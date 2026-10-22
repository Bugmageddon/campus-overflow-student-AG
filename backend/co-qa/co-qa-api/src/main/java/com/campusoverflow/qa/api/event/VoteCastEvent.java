package com.campusoverflow.qa.api.event;

import com.campusoverflow.shared.event.IntegrationEvent;
import java.time.Instant;

/**
 * 投票变化。oldValue/newValue ∈ {-1, 0, 1}，消费者据此计算增量（撤销、改投都可正确处理）。
 * targetType 取值 QUESTION / ANSWER。
 */
public record VoteCastEvent(String eventId, Instant occurredAt, String targetType, long targetId, long targetAuthorId,
                            long voterId, long questionId, int oldValue, int newValue) implements IntegrationEvent {
}
