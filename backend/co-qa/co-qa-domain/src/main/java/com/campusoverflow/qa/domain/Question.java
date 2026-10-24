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
        return new Question(null, authorId, courseId, title, body, tags, null, 0, 0, 0, now, now, null, 0L);
    }

    public static Question reconstitute(Long id, long authorId, Long courseId, Title title, MarkdownBody body,
                                        Tags tags, Long acceptedAnswerId, int score, int answerCount, int viewCount,
                                        Instant createdAt, Instant updatedAt, Instant deletedAt, long version) {
        return new Question(id, authorId, courseId, title, body, tags, acceptedAnswerId, score, answerCount,
                viewCount, createdAt, updatedAt, deletedAt, version);
    }

    public void edit(Actor actor, boolean actorManagesCourse, Title newTitle, MarkdownBody newBody, Tags newTags,
                     Instant now) {
        ensureNotDeleted();
        if (actor.userId() != authorId && !actorManagesCourse) {
            throw new ForbiddenException("QA-3002", "只有作者或课程管理者可以编辑问题");
        }
        this.title = newTitle;
        this.body = newBody;
        this.tags = newTags;
        this.updatedAt = now;
    }

    /** 采纳最佳答案。 */
    public void accept(Answer answer, Actor actor, boolean actorManagesCourse) {
        ensureNotDeleted();
        if (actor.userId() != authorId && !actorManagesCourse) {
            throw new ForbiddenException("QA-3001", "只有提问者或课程教师/助教可以采纳");
        }
        if (acceptedAnswerId != null) {
            throw new ConflictException("QA-2002", "问题已采纳，不能重复采纳");
        }
        if (answer.questionId() != id) {
            throw new BusinessRuleException("QA-1010", "答案不属于该问题");
        }
        if (answer.isDeleted()) {
            throw new BusinessRuleException("QA-1011", "已删除的回答不能被采纳");
        }
        this.acceptedAnswerId = answer.id();
    }

    public void softDelete(Actor actor, boolean actorManagesCourse, Instant now) {
        ensureNotDeleted();
        boolean author = actor.userId() == authorId;
        if (!author && !actorManagesCourse) {
            throw new ForbiddenException("QA-3003", "无权删除该问题");
        }
        if (author && !actorManagesCourse && answerCount > 0) {
            throw new BusinessRuleException("QA-1013", "已有回答的问题，提问者不能删除");
        }
        this.deletedAt = now;
        this.updatedAt = now;
    }

    public void ensureOpenForAnswers() {
        if (isDeleted()) {
            throw new BusinessRuleException("QA-1020", "内容已删除，不能回答");
        }
    }

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new BusinessRuleException("QA-1020", "内容已删除");
        }
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
