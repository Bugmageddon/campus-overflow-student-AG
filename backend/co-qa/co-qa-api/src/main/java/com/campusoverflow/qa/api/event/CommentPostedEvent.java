package com.campusoverflow.qa.api.event;

import com.campusoverflow.shared.event.IntegrationEvent;
import java.time.Instant;

public record CommentPostedEvent(String eventId, Instant occurredAt, long commentId, long questionId, long authorId,
                                 String body) implements IntegrationEvent {
}
