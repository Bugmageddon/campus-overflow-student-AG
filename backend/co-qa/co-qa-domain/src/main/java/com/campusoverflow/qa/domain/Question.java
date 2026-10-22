package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.ConflictException;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.security.Actor;
import java.time.Instant;
import java.util.Objects;

/**
 * 问题聚合根——问答核心不变量的守护者。
 * <ul>
 *   <li>最多只有一个被采纳的答案，且采纳后不可更改；</li>
 *   <li>只有提问者或课程管理者（教师/助教/管理员）可以采纳；</li>
 *   <li>已删除的问题不能再被回答、编辑或采纳；</li>
 *   <li>提问者只能删除尚无回答的问题，课程管理者可删除任意问题。</li>
 * </ul>
 * score / answerCount / viewCount 为反规范化计数器，由仓储以原子 SQL 维护，不在此处修改。
 */
public class Question {

    private Long id;
    private final long authorId;
    private final Long courseId;
    private Title title;
    private MarkdownBody body;
    private Tags tags;
    private Long acceptedAnswerId;
    private final int score;
    private final int answerCount;
    private final int viewCount;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private final long version;

    private Question(Long id, long authorId, Long courseId, Title title, MarkdownBody body, Tags tags,
                     Long acceptedAnswerId, int score, int answerCount, int viewCount, Instant createdAt,
                     Instant updatedAt, Instant deletedAt, long version) {
        this.id = id;
        this.authorId = authorId;
        this.courseId = courseId;
        this.title = Objects.requireNonNull(title);
        this.body = Objects.requireNonNull(body);
        this.tags = Objects.requireNonNull(tags);
        this.acceptedAnswerId = acceptedAnswerId;
        this.score = score;
        this.answerCount = answerCount;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    public static Question post(long authorId, Long courseId, Title title, MarkdownBody body, Tags tags, Instant now) {
        // TODO(S2)：实现 Question.post——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Question.post 尚未实现");
    }

    public static Question reconstitute(Long id, long authorId, Long courseId, Title title, MarkdownBody body,
                                        Tags tags, Long acceptedAnswerId, int score, int answerCount, int viewCount,
                                        Instant createdAt, Instant updatedAt, Instant deletedAt, long version) {
        return new Question(id, authorId, courseId, title, body, tags, acceptedAnswerId, score, answerCount,
                viewCount, createdAt, updatedAt, deletedAt, version);
    }

    public void edit(Actor actor, boolean actorManagesCourse, Title newTitle, MarkdownBody newBody, Tags newTags,
                     Instant now) {
        // TODO(S2)：实现 Question.edit——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Question.edit 尚未实现");
    }

    /** 采纳最佳答案。 */
    public void accept(Answer answer, Actor actor, boolean actorManagesCourse) {
        // TODO(S2)：实现 Question.accept——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Question.accept 尚未实现");
    }

    public void softDelete(Actor actor, boolean actorManagesCourse, Instant now) {
        // TODO(S2)：实现 Question.softDelete——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Question.softDelete 尚未实现");
    }

    public void ensureOpenForAnswers() {
        // TODO(S2)：实现 Question.ensureOpenForAnswers——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Question.ensureOpenForAnswers 尚未实现");
    }

    private void ensureNotDeleted() {
        // TODO(S2)：实现 Question.ensureNotDeleted——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Question.ensureNotDeleted 尚未实现");
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isAccepted(long answerId) {
        return acceptedAnswerId != null && acceptedAnswerId == answerId;
    }

    public void assignId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("ID 已分配");
        }
        this.id = id;
    }

    public Long id() { return id; }
    public long authorId() { return authorId; }
    public Long courseId() { return courseId; }
    public Title title() { return title; }
    public MarkdownBody body() { return body; }
    public Tags tags() { return tags; }
    public Long acceptedAnswerId() { return acceptedAnswerId; }
    public int score() { return score; }
    public int answerCount() { return answerCount; }
    public int viewCount() { return viewCount; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Instant deletedAt() { return deletedAt; }
    public long version() { return version; }
}
