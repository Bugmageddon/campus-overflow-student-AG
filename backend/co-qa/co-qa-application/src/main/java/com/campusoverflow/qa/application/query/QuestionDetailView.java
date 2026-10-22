package com.campusoverflow.qa.application.query;

import java.time.Instant;
import java.util.List;

public record QuestionDetailView(long id, String title, String body, String bodyHtml, List<String> tags,
                                 CourseRefView course, AuthorView author, int score, int answerCount, int viewCount,
                                 Long acceptedAnswerId, int myVote, Instant createdAt, Instant updatedAt,
                                 PermissionsView permissions, List<CommentView> comments, List<AnswerView> answers) {
}
