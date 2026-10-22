package com.campusoverflow.qa.api.event;

import com.campusoverflow.shared.event.IntegrationEvent;
import java.time.Instant;

public record AnswerSubmittedEvent(String eventId, Instant occurredAt, long answerId, long questionId,
                                   long answerAuthorId, long questionAuthorId, String questionTitle)
        implements IntegrationEvent {
}
