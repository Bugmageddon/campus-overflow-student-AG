package com.campusoverflow.qa.api.event;

import com.campusoverflow.shared.event.IntegrationEvent;
import java.time.Instant;

/** 问题或回答被软删除。targetType 取值 QUESTION / ANSWER。 */
public record ContentDeletedEvent(String eventId, Instant occurredAt, String targetType, long targetId,
                                  long questionId) implements IntegrationEvent {
}
