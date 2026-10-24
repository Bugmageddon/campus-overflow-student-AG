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
        if (voterId == targetAuthorId) {
            throw new BusinessRuleException("QA-1015", "不能给自己的内容投票");
        }
        checkValue(value);
        return new Vote(null, voterId, targetType, targetId, value, now);
    }

    public static Vote reconstitute(Long id, long voterId, TargetType targetType, long targetId, int value,
                                    Instant updatedAt) {
        return new Vote(id, voterId, targetType, targetId, value, updatedAt);
    }

    /** 改投，返回原值。 */
    public int change(int newValue, Instant now) {
        checkValue(newValue);
        int old = value;
        this.value = newValue;
        this.updatedAt = now;
        return old;
    }

    private static int checkValue(int value) {
        if (value != -1 && value != 0 && value != 1) {
            throw new BusinessRuleException("QA-1014", "投票值只能为 -1、0（撤销）、+1");
        }
        return value;
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
