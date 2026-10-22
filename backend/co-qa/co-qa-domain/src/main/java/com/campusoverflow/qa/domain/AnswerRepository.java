package com.campusoverflow.qa.domain;

import java.util.Optional;

public interface AnswerRepository {
    Optional<Answer> findById(long id);

    Answer save(Answer answer);

    void adjustScore(long answerId, int delta);
}
