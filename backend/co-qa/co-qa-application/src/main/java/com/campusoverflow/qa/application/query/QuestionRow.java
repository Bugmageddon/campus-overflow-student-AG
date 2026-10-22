package com.campusoverflow.qa.application.query;

import java.time.Instant;
import java.util.List;

public record QuestionRow(long id, long authorId, Long courseId, String title, String body, List<String> tags,
                          Long acceptedAnswerId, int score, int answerCount, int viewCount, Instant createdAt,
                          Instant updatedAt) {
}
