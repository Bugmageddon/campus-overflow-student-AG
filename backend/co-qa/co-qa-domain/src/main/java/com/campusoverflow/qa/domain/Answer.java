package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.ConflictException;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.security.Actor;
import java.time.Instant;
import java.util.Objects;

/**
 * 回答聚合根。独立于 Question 聚合以降低并发冲突，通过 questionId 引用问题。
 * “是否被采纳”由 Question.acceptedAnswerId 决定（单一事实来源），这里不重复存储。
 */
public class Answer {

    private Long id;
    private final long questionId;
    private final long authorId;
    private MarkdownBody body;
    private final int score;
    private Long endorsedBy;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private final long version;

    private Answer(Long id, long questionId, long authorId, MarkdownBody body, int score, Long endorsedBy,
                   Instant createdAt, Instant updatedAt, Instant deletedAt, long version) {
        this.id = id;
        this.questionId = questionId;
        this.authorId = authorId;
        this.body = Objects.requireNonNull(body);
        this.score = score;
        this.endorsedBy = endorsedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    public static Answer submit(Question question, long authorId, MarkdownBody body, Instant now) {
        // TODO(S2)：实现 Answer.submit——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Answer.submit 尚未实现");
    }

    public static Answer reconstitute(Long id, long questionId, long authorId, MarkdownBody body, int score,
                                      Long endorsedBy, Instant createdAt, Instant updatedAt, Instant deletedAt,
                                      long version) {
        return new Answer(id, questionId, authorId, body, score, endorsedBy, createdAt, updatedAt, deletedAt, version);
    }

    public void edit(Actor actor, boolean actorManagesCourse, MarkdownBody newBody, Instant now) {
        // TODO(S2)：实现 Answer.edit——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Answer.edit 尚未实现");
    }

    /** 教师认证：课程教师/助教为优质回答打上“教师认证”标记。 */
    public void endorse(Actor actor, boolean actorManagesCourse) {
        // TODO(S2)：实现 Answer.endorse——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Answer.endorse 尚未实现");
    }

    /** 被采纳的回答不能由回答者本人删除（避免“先骗采纳再删除”），课程管理者可删除。 */
    public void softDelete(Actor actor, boolean actorManagesCourse, boolean accepted, Instant now) {
        // TODO(S2)：实现 Answer.softDelete——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Answer.softDelete 尚未实现");
    }

    private void ensureNotDeleted() {
        // TODO(S2)：实现 Answer.ensureNotDeleted——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Answer.ensureNotDeleted 尚未实现");
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void assignId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("ID 已分配");
        }
        this.id = id;
    }

    public Long id() { return id; }
    public long questionId() { return questionId; }
    public long authorId() { return authorId; }
    public MarkdownBody body() { return body; }
    public int score() { return score; }
    public Long endorsedBy() { return endorsedBy; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Instant deletedAt() { return deletedAt; }
    public long version() { return version; }
}
