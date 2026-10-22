package com.campusoverflow.reputation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.ConflictException;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BountyAndAccountTest {

    private static final Instant NOW = Instant.parse("2026-10-08T02:00:00Z");
    private final Actor teacher = new Actor(9, "王老师", Role.TEACHER);

    @Test
    void onlyCourseManagerCanOpenBountyWithinLimits() {
        assertThatThrownBy(() -> Bounty.open(10, true, new Actor(1, "学生", Role.STUDENT), false, 50, 7, NOW))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> Bounty.open(10, true, teacher, true, 5, 7, NOW))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> Bounty.open(10, false, teacher, true, 50, 7, NOW))
                .isInstanceOf(BusinessRuleException.class);
        Bounty b = Bounty.open(10, true, teacher, true, 50, 7, NOW);
        assertThat(b.expiresAt()).isEqualTo(NOW.plus(Duration.ofDays(7)));
        assertThat(b.status()).isEqualTo(BountyStatus.OPEN);
    }

    @Test
    void bountyCanBeSettledOnlyOnce() {
        Bounty b = Bounty.open(10, true, teacher, true, 50, 7, NOW);
        b.award(50, 3, NOW.plusSeconds(60));
        assertThat(b.status()).isEqualTo(BountyStatus.AWARDED);
        assertThat(b.winnerId()).isEqualTo(3L);
        assertThatThrownBy(() -> b.close(NOW)).isInstanceOf(ConflictException.class);
    }

    @Test
    void sponsorCannotWinOwnBountyAndDueDetectionWorks() {
        Bounty b = Bounty.open(10, true, teacher, true, 50, 1, NOW);
        assertThatThrownBy(() -> b.award(50, 9, NOW)).isInstanceOf(BusinessRuleException.class);
        assertThat(b.isDue(NOW)).isFalse();
        assertThat(b.isDue(NOW.plus(Duration.ofDays(1)))).isTrue();
    }

    @Test
    void freezeSpendAndRefundKeepPointsConsistent() {
        ReputationAccount acc = ReputationAccount.open(9, 1000);
        acc.freezePoints(300);
        assertThat(acc.availablePoints()).isEqualTo(700);
        assertThat(acc.frozenPoints()).isEqualTo(300);
        acc.spendFrozen(100);
        acc.refundFrozen(200);
        assertThat(acc.availablePoints()).isEqualTo(900);
        assertThat(acc.frozenPoints()).isZero();
        assertThatThrownBy(() -> acc.freezePoints(901)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void badgePolicyThresholds() {
        assertThat(BadgePolicy.forAcceptedCount(0)).isEmpty();
        assertThat(BadgePolicy.forAcceptedCount(10)).containsExactlyInAnyOrder(BadgeType.FIRST_ACCEPTED, BadgeType.HELPER_10);
        assertThat(BadgePolicy.forReputation(150)).containsExactly(BadgeType.REPUTATION_100);
    }

    @Test
    void academicTermKeysFollowCampusCalendar() {
        assertThat(AcademicTerm.termKey(Instant.parse("2026-10-08T02:00:00Z"))).isEqualTo("2026-FALL");
        assertThat(AcademicTerm.termKey(Instant.parse("2027-01-10T02:00:00Z"))).isEqualTo("2026-FALL");
        assertThat(AcademicTerm.termKey(Instant.parse("2027-03-01T02:00:00Z"))).isEqualTo("2027-SPRING");
        assertThat(AcademicTerm.monthKey(Instant.parse("2026-10-08T02:00:00Z"))).isEqualTo("2026-10");
        assertThat(AcademicTerm.weekKey(Instant.parse("2026-10-08T02:00:00Z"))).isEqualTo("2026-W41");
    }
}
