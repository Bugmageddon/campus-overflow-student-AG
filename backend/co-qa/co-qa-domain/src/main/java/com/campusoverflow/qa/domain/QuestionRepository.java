package com.campusoverflow.qa.domain;

import java.util.Optional;

public interface QuestionRepository {
    Optional<Question> findById(long id);

    Question save(Question question);

    /** 原子地调整回答数（UPDATE ... SET answer_count = answer_count + ?）。 */
    void adjustAnswerCount(long questionId, int delta);

    /** 原子地调整得分。 */
    void adjustScore(long questionId, int delta);
}
