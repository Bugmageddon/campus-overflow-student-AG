package com.campusoverflow.qa.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.shared.domain.BusinessRuleException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class VoteAndCommentTest {

    private static final Instant NOW = Instant.parse("2026-10-01T08:00:00Z");

    @Test
    void cannotVoteOnOwnContent() {
        assertThatThrownBy(() -> Vote.cast(3, 3, TargetType.ANSWER, 50, 1, NOW))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("自己");
    }

    @Test
    void voteValueMustBePlusOrMinusOne() {
        assertThatThrownBy(() -> Vote.cast(1, 3, TargetType.ANSWER, 50, 2, NOW))
                .isInstanceOf(BusinessRuleException.class);
        Vote v = Vote.cast(1, 3, TargetType.ANSWER, 50, 1, NOW);
        assertThat(v.change(-1, NOW)).isEqualTo(1);
        assertThat(v.value()).isEqualTo(-1);
    }

    @Test
    void repliesAreLimitedToOneLevelOnSameTarget() {
        Comment top = Comment.post(TargetType.ANSWER, 50, 10, 1, null, "请问第二步为什么要变色？", NOW);
        top.assignId(1);
        Comment reply = Comment.post(TargetType.ANSWER, 50, 10, 3, top, "因为要保持黑高一致。", NOW);
        reply.assignId(2);
        assertThat(reply.parentId()).isEqualTo(1L);

        assertThatThrownBy(() -> Comment.post(TargetType.ANSWER, 50, 10, 1, reply, "再回复一层", NOW))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> Comment.post(TargetType.QUESTION, 10, 10, 1, top, "挂错位置的回复", NOW))
                .isInstanceOf(BusinessRuleException.class);
    }
}
