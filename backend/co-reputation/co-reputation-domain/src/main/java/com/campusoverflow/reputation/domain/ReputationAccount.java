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
        // TODO(S3)：实现 ReputationAccount.open——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReputationAccount.open 尚未实现");
    }

    public static ReputationAccount reconstitute(long userId, int reputation, int availablePoints, int frozenPoints,
                                                 long version) {
        return new ReputationAccount(userId, reputation, availablePoints, frozenPoints, version);
    }

    /** 只允许通过账本流水改变声誉（由应用服务保证先写流水）。 */
    public void applyReputation(int delta) {
        // TODO(S3)：实现 ReputationAccount.applyReputation——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReputationAccount.applyReputation 尚未实现");
    }

    public void freezePoints(int points) {
        // TODO(S3)：实现 ReputationAccount.freezePoints——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReputationAccount.freezePoints 尚未实现");
    }

    /** 悬赏成功发放：冻结积分转出给获奖者（以声誉形式），发起人账户中消失。 */
    public void spendFrozen(int points) {
        // TODO(S3)：实现 ReputationAccount.spendFrozen——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReputationAccount.spendFrozen 尚未实现");
    }

    /** 悬赏到期：冻结积分退回可用。 */
    public void refundFrozen(int points) {
        // TODO(S3)：实现 ReputationAccount.refundFrozen——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReputationAccount.refundFrozen 尚未实现");
    }

    private void ensureFrozen(int points) {
        // TODO(S3)：实现 ReputationAccount.ensureFrozen——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReputationAccount.ensureFrozen 尚未实现");
    }

    private static void requirePositive(int points) {
        // TODO(S3)：实现 ReputationAccount.requirePositive——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReputationAccount.requirePositive 尚未实现");
    }

    public long userId() { return userId; }
    public int reputation() { return reputation; }
    public int availablePoints() { return availablePoints; }
    public int frozenPoints() { return frozenPoints; }
    public long version() { return version; }
}
