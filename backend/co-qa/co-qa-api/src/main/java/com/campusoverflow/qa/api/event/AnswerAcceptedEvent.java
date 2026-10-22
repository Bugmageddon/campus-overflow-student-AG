package com.campusoverflow.qa.api.event;

import com.campusoverflow.shared.event.IntegrationEvent;
import java.time.Instant;

/** 最佳答案被采纳。Reputation 订阅以结算声誉（+15）与悬赏；Discovery 订阅以更新索引与发送通知。 */
public record AnswerAcceptedEvent(String eventId, Instant occurredAt, long answerId, long questionId,
                                  long answerAuthorId, long questionAuthorId, long acceptedById, String questionTitle)
        implements IntegrationEvent {
}
