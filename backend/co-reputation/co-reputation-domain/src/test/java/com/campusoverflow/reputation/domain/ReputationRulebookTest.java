package com.campusoverflow.reputation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusoverflow.shared.security.Role;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class ReputationRulebookTest {

    @ParameterizedTest(name = "{0} → {1} 的声誉增量为 {2}")
    @CsvSource({
            "0, 1, 10",    // 新赞
            "0, -1, -2",   // 新踩
            "1, 0, -10",   // 撤销赞
            "-1, 0, 2",    // 撤销踩
            "1, -1, -12",  // 赞改踩
            "-1, 1, 12"    // 踩改赞
    })
    void voteDeltaIsNewEffectMinusOldEffect(int oldValue, int newValue, int expected) {
        assertThat(ReputationRulebook.voteDelta(oldValue, newValue)).isEqualTo(expected);
    }

    @Test
    void selfAcceptedAnswerEarnsNothing() {
        assertThat(ReputationRulebook.acceptedDelta(7, 7)).isZero();
        assertThat(ReputationRulebook.acceptedDelta(7, 8)).isEqualTo(15);
    }

    @Test
    void dailyCapLimitsOnlyPositiveVoteGains() {
        assertThat(ReputationRulebook.applyDailyCap(10, 195)).isEqualTo(5);
        assertThat(ReputationRulebook.applyDailyCap(10, 200)).isZero();
        assertThat(ReputationRulebook.applyDailyCap(-2, 500)).isEqualTo(-2);
    }

    @Test
    void teachersStartWithBountyPoints() {
        assertThat(ReputationRulebook.initialPoints(Role.TEACHER)).isEqualTo(1000);
        assertThat(ReputationRulebook.initialPoints(Role.STUDENT)).isZero();
    }
}
