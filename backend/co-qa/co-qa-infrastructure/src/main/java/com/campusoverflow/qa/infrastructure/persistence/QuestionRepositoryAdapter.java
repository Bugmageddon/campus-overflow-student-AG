package com.campusoverflow.qa.infrastructure.persistence;

import com.campusoverflow.qa.domain.MarkdownBody;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.QuestionRepository;
import com.campusoverflow.qa.domain.Tags;
import com.campusoverflow.qa.domain.Title;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Question 仓储适配器。注意：更新时<strong>不回写</strong> score/answerCount/viewCount 三个计数器，
 * 它们只由原子 UPDATE 维护，防止“读-改-写”覆盖并发增量。
 */
@Repository
public class QuestionRepositoryAdapter implements QuestionRepository {

    private final QuestionJpaRepository jpa;

    public QuestionRepositoryAdapter(QuestionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Question> findById(long id) {
        return jpa.findById(id).map(QuestionRepositoryAdapter::toDomain);
    }

    @Override
    public Question save(Question q) {
        QuestionJpaEntity e = q.id() == null
                ? new QuestionJpaEntity(q.authorId(), q.courseId(), q.createdAt())
                : jpa.findById(q.id()).orElseThrow();
        e.setTitle(q.title().value());
        e.setBody(q.body().value());
        e.getTags().clear();
        e.getTags().addAll(q.tags().names());
        e.setAcceptedAnswerId(q.acceptedAnswerId());
        e.setUpdatedAt(q.updatedAt());
        e.setDeletedAt(q.deletedAt());
        QuestionJpaEntity saved = jpa.save(e);
        if (q.id() == null) {
            q.assignId(saved.getId());
        }
        return q;
    }

    @Override
    public void adjustAnswerCount(long questionId, int delta) {
        jpa.adjustAnswerCount(questionId, delta);
    }

    @Override
    public void adjustScore(long questionId, int delta) {
        jpa.adjustScore(questionId, delta);
    }

    static Question toDomain(QuestionJpaEntity e) {
        return Question.reconstitute(e.getId(), e.getAuthorId(), e.getCourseId(), new Title(e.getTitle()),
                new MarkdownBody(e.getBody()), Tags.of(List.copyOf(e.getTags())), e.getAcceptedAnswerId(), e.getScore(),
                e.getAnswerCount(), e.getViewCount(), e.getCreatedAt(), e.getUpdatedAt(), e.getDeletedAt(),
                e.getVersion());
    }
}
