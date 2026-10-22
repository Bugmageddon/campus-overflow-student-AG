package com.campusoverflow.qa.domain;

import java.util.Optional;

public interface CommentRepository {
    Optional<Comment> findById(long id);

    Comment save(Comment comment);
}
