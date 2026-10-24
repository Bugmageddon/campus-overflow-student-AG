package com.campusoverflow.qa.application;

import com.campusoverflow.qa.api.QaQueryApi;
import com.campusoverflow.qa.api.QuestionRef;
import com.campusoverflow.qa.domain.QuestionRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QaQueryApiImpl implements QaQueryApi {

    private final QuestionRepository questions;

    public QaQueryApiImpl(QuestionRepository questions) {
        this.questions = questions;
    }

    @Override
    public Optional<QuestionRef> findQuestion(long questionId) {
        return questions.findById(questionId)
                .map(question -> new QuestionRef(question.id(), question.authorId(), question.courseId(),
                        question.title().value(), question.acceptedAnswerId(), question.isDeleted()));
    }
}
