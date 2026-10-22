package com.campusoverflow.qa.api.event;

import com.campusoverflow.shared.event.IntegrationEvent;
import java.time.Instant;
import java.util.List;

public record QuestionPostedEvent(String eventId, Instant occurredAt, long questionId, long authorId, Long courseId,
                                  String title, String plainText, List<String> tags) implements IntegrationEvent {
}
