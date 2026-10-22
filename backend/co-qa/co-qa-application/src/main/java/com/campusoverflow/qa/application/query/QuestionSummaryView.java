package com.campusoverflow.qa.application.query;

import java.time.Instant;
import java.util.List;

public record QuestionSummaryView(long id, String title, String excerpt, List<String> tags, Long courseId,
                                  AuthorView author, int score, int answerCount, int viewCount, boolean accepted,
                                  Instant createdAt) {
}
