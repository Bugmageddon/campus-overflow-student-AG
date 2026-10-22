package com.campusoverflow.reputation.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.ConflictException;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.security.Actor;
import java.time.Duration;
import java.time.Instant;

/**
 * 悬赏：OPEN →（问题采纳）→ AWARDED；OPEN →（到期）→ EXPIRED。终态不可再变。
 */
public class Bounty {

    public static final int MIN_POINTS = 10;
    public static final int MAX_POINTS = 500;
    public static final int MAX_DAYS = 14;

    private Long id;
    private final long questionId;
    private final long sponsorId;
    private final int points;
    private BountyStatus status;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Long winnerId;
    private Long answerId;
    private Instant closedAt;

    private Bounty(Long id, long questionId, long sponsorId, int points, BountyStatus status, Instant createdAt,
                   Instant expiresAt, Long winnerId, Long answerId, Instant closedAt) {
        this.id = id;
        this.questionId = questionId;
        this.sponsorId = sponsorId;
        this.points = points;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.winnerId = winnerId;
        this.answerId = answerId;
        this.closedAt = closedAt;
    }

    /**
     * 发起悬赏。发起人须为课程管理者（教师/助教/管理员）；问题不能已删除或已采纳。
     */
    public static Bounty open(long questionId, boolean questionOpen, Actor sponsor, boolean sponsorManagesCourse,
                              int points, int days, Instant now) {
        // TODO(S3)：实现 Bounty.open——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：Bounty.open 尚未实现");
    }

    public static Bounty reconstitute(Long id, long questionId, long sponsorId, int points, BountyStatus status,
                                      Instant createdAt, Instant expiresAt, Long winnerId, Long answerId,
                                      Instant closedAt) {
        return new Bounty(id, questionId, sponsorId, points, status, createdAt, expiresAt, winnerId, answerId,
                closedAt);
    }

    public void award(long answerId, long winnerId, Instant now) {
        // TODO(S3)：实现 Bounty.award——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：Bounty.award 尚未实现");
    }

    /** 关闭并退款：到期未采纳，或被采纳的恰好是发起人自己的回答。 */
    public void close(Instant now) {
        // TODO(S3)：实现 Bounty.close——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：Bounty.close 尚未实现");
    }

    public boolean isDue(Instant now) {
        // TODO(S3)：实现 Bounty.isDue——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：Bounty.isDue 尚未实现");
    }

    private void ensureOpen() {
        // TODO(S3)：实现 Bounty.ensureOpen——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：Bounty.ensureOpen 尚未实现");
    }

    public void assignId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("ID 已分配");
        }
        this.id = id;
    }

    public Long id() { return id; }
    public long questionId() { return questionId; }
    public long sponsorId() { return sponsorId; }
    public int points() { return points; }
    public BountyStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant expiresAt() { return expiresAt; }
    public Long winnerId() { return winnerId; }
    public Long answerId() { return answerId; }
    public Instant closedAt() { return closedAt; }
}
