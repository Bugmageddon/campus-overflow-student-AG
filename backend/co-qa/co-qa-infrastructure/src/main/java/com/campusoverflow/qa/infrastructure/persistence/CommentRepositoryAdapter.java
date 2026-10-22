package com.campusoverflow.qa.infrastructure.persistence;

import com.campusoverflow.qa.domain.Comment;
import com.campusoverflow.qa.domain.CommentRepository;
import com.campusoverflow.qa.domain.TargetType;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CommentRepositoryAdapter implements CommentRepository {

    private final CommentJpaRepository jpa;

    public CommentRepositoryAdapter(CommentJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Comment> findById(long id) {
        return jpa.findById(id).map(e -> Comment.reconstitute(e.getId(), TargetType.valueOf(e.getTargetType()),
                e.getTargetId(), e.getQuestionId(), e.getAuthorId(), e.getParentId(), e.getBody(), e.getCreatedAt()));
    }

    @Override
    public Comment save(Comment c) {
        if (c.id() != null) {
            return c; // 评论不可编辑
        }
        CommentJpaEntity saved = jpa.save(new CommentJpaEntity(c.targetType().name(), c.targetId(), c.questionId(),
                c.authorId(), c.parentId(), c.body(), c.createdAt()));
        c.assignId(saved.getId());
        return c;
    }
}
