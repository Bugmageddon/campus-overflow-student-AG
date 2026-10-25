package com.campusoverflow.reputation.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;

/**
 * 声誉账户。不变量：reputation 恒等于该用户所有账本流水 delta 之和（由对账任务 FF-8 验证）。
 * 悬赏积分 = 可用 + 冻结，冻结部分在悬赏结算或到期时释放。
 */
public class ReputationAccount {

    private final long userId;
    private int reputation;
    private int availablePoints;
    private int frozenPoints;
    private final long version;

    private ReputationAccount(long userId, int reputation, int availablePoints, int frozenPoints, long version) {
        this.userId = userId;
        this.reputation = reputation;
        this.availablePoints = availablePoints;
        this.frozenPoints = frozenPoints;
        this.version = version;
    }

    public static ReputationAccount open(long userId, int initialPoints) {
        if (initialPoints < 0) {
            throw new BusinessRuleException("REP-1006", "开户积分不能为负");
        }
        return new ReputationAccount(userId, 0, initialPoints, 0, 0);
    }

    public static ReputationAccount reconstitute(long userId, int reputation, int availablePoints, int frozenPoints,
                                                 long version) {
        return new ReputationAccount(userId, reputation, availablePoints, frozenPoints, version);
    }

    /** 只允许通过账本流水改变声誉（由应用服务保证先写流水）。 */
    public void applyReputation(int delta) {
        // 不设下限截断，保证“余额 = 流水之和”不变量（FF-8）
        this.reputation += delta;
    }

    public void freezePoints(int points) {
        requirePositive(points);
        if (points > availablePoints) {
            throw new BusinessRuleException("REP-1002", "可用悬赏积分不足");
        }
        this.availablePoints -= points;
        this.frozenPoints += points;
    }

    /** 悬赏成功发放：冻结积分转出给获奖者（以声誉形式），发起人账户中消失。 */
    public void spendFrozen(int points) {
        requirePositive(points);
        ensureFrozen(points);
        this.frozenPoints -= points;
    }

    /** 悬赏到期：冻结积分退回可用。 */
    public void refundFrozen(int points) {
        requirePositive(points);
        ensureFrozen(points);
        this.frozenPoints -= points;
        this.availablePoints += points;
    }

    private void ensureFrozen(int points) {
        if (points > frozenPoints) {
            throw new BusinessRuleException("REP-1002", "冻结积分不足");
        }
    }

    private static void requirePositive(int points) {
        if (points <= 0) {
            throw new BusinessRuleException("REP-1006", "悬赏积分必须为正");
        }
    }

    public long userId() { return userId; }
    public int reputation() { return reputation; }
    public int availablePoints() { return availablePoints; }
    public int frozenPoints() { return frozenPoints; }
    public long version() { return version; }
}
