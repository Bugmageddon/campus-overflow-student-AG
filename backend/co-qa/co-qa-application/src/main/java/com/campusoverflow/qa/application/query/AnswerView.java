package com.campusoverflow.qa.application.query;

import java.time.Instant;
import java.util.List;

public record AnswerView(long id, String body, String bodyHtml, AuthorView author, int score, boolean accepted,
                         AuthorView endorsedBy, int myVote, boolean canEdit, Instant createdAt, Instant updatedAt,
                         List<CommentView> comments) {
}
