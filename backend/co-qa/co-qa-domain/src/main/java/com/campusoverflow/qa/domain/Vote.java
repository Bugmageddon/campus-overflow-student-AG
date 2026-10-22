package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;
import java.time.Instant;

/** 投票聚合：同一用户对同一内容仅一票（数据库唯一约束兜底），值为 +1 或 -1；撤销即删除。 */
public class Vote {

    private Long id;
    private final long voterId;
    private final TargetType targetType;
    private final long targetId;
    private int value;
    private Instant updatedAt;

    private Vote(Long id, long voterId, TargetType targetType, long targetId, int value, Instant updatedAt) {
        this.id = id;
        this.voterId = voterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.value = value;
        this.updatedAt = updatedAt;
    }

    public static Vote cast(long voterId, long targetAuthorId, TargetType targetType, long targetId, int value,
                            Instant now) {
        // TODO(S2)：实现 Vote.cast——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Vote.cast 尚未实现");
    }

    public static Vote reconstitute(Long id, long voterId, TargetType targetType, long targetId, int value,
                                    Instant updatedAt) {
        return new Vote(id, voterId, targetType, targetId, value, updatedAt);
    }

    /** 改投，返回原值。 */
    public int change(int newValue, Instant now) {
        // TODO(S2)：实现 Vote.change——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Vote.change 尚未实现");
    }

    private static int checkValue(int value) {
        // TODO(S2)：实现 Vote.checkValue——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：Vote.checkValue 尚未实现");
    }

    public void assignId(long id) {
        this.id = id;
    }

    public Long id() { return id; }
    public long voterId() { return voterId; }
    public TargetType targetType() { return targetType; }
    public long targetId() { return targetId; }
    public int value() { return value; }
    public Instant updatedAt() { return updatedAt; }
}
