package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.Guard;
import java.time.Instant;

/** 评论：挂在问题或回答下，支持一层“二级回复”。 */
public class Comment {

    private Long id;
    private final TargetType targetType;
    private final long targetId;
    private final long questionId;
    private final long authorId;
    private final Long parentId;
    private final String body;
    private final Instant createdAt;

    private Comment(Long id, TargetType targetType, long targetId, long questionId, long authorId, Long parentId,
                    String body, Instant createdAt) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.questionId = questionId;
        this.authorId = authorId;
        this.parentId = parentId;
        this.body = body;
        this.createdAt = createdAt;
    }

    public static Comment post(TargetType targetType, long targetId, long questionId, long authorId, Comment parent,
                               String body, Instant now) {
        // TODO(S2)：实现 Comment.post——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Comment.post 尚未实现");
    }

    public static Comment reconstitute(Long id, TargetType targetType, long targetId, long questionId, long authorId,
                                       Long parentId, String body, Instant createdAt) {
        return new Comment(id, targetType, targetId, questionId, authorId, parentId, body, createdAt);
    }

    public void assignId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("ID 已分配");
        }
        this.id = id;
    }

    public Long id() { return id; }
    public TargetType targetType() { return targetType; }
    public long targetId() { return targetId; }
    public long questionId() { return questionId; }
    public long authorId() { return authorId; }
    public Long parentId() { return parentId; }
    public String body() { return body; }
    public Instant createdAt() { return createdAt; }
}
