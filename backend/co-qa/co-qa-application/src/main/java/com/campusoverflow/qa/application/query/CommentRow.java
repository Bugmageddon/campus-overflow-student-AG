package com.campusoverflow.qa.application.query;

import java.time.Instant;

public record CommentRow(long id, String targetType, long targetId, long authorId, Long parentId, String body,
                         Instant createdAt) {
}
