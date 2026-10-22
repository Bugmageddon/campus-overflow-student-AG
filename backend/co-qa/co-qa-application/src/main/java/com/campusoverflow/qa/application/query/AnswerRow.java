package com.campusoverflow.qa.application.query;

import java.time.Instant;

public record AnswerRow(long id, long authorId, String body, int score, Long endorsedBy, Instant createdAt,
                        Instant updatedAt) {
}
