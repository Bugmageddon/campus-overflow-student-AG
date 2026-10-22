package com.campusoverflow.reputation.domain;

import com.campusoverflow.shared.security.Role;

/**
 * 声誉规则手册——把“游戏规则”集中在一处，便于课堂讨论与参数调整。
 * <ul>
 *   <li>回答被采纳 +15（自问自答被采纳不加分）；</li>
 *   <li>被赞 +10，被踩 −2；改投、撤销按“新效果 − 旧效果”计算增量；</li>
 *   <li>反作弊：每人每个自然日（UTC）因被投票获得的正向声誉最多 200。</li>
 * </ul>
 */
public final class ReputationRulebook {

    public static final int ACCEPTED = 15;
    public static final int UPVOTE = 10;
    public static final int DOWNVOTE = -2;
    public static final int DAILY_VOTE_CAP = 200;

    private ReputationRulebook() {
    }

    public static int voteEffect(int value) {
        // TODO(S3)：实现 ReputationRulebook.voteEffect——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReputationRulebook.voteEffect 尚未实现");
    }

    public static int voteDelta(int oldValue, int newValue) {
        // TODO(S3)：实现 ReputationRulebook.voteDelta——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReputationRulebook.voteDelta 尚未实现");
    }

    public static int acceptedDelta(long answerAuthorId, long questionAuthorId) {
        return answerAuthorId == questionAuthorId ? 0 : ACCEPTED;
    }

    /** 对正向投票增量应用每日上限；负向增量不受限。 */
    public static int applyDailyCap(int delta, int positiveVoteGainToday) {
        // TODO(S3)：实现 ReputationRulebook.applyDailyCap——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReputationRulebook.applyDailyCap 尚未实现");
    }

    /** 开户时发放的悬赏积分（与声誉是两种不同的“货币”）。 */
    public static int initialPoints(Role role) {
        // TODO(S3)：实现 ReputationRulebook.initialPoints——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReputationRulebook.initialPoints 尚未实现");
    }
}
