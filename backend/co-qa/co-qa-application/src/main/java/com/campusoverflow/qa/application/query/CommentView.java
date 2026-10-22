package com.campusoverflow.qa.application.query;

import java.time.Instant;

public record CommentView(long id, AuthorView author, Long parentId, String body, Instant createdAt) {
}
