package com.campusoverflow.qa.api.event;

import com.campusoverflow.shared.event.IntegrationEvent;
import java.time.Instant;

/** 教师认证了一个回答（“教师认证”标记）。 */
public record AnswerEndorsedEvent(String eventId, Instant occurredAt, long answerId, long questionId,
                                  long answerAuthorId, long endorserId, String questionTitle)
        implements IntegrationEvent {
}
