package com.campusoverflow.reputation.application;

import com.campusoverflow.reputation.api.event.BadgeAwardedEvent;
import com.campusoverflow.reputation.domain.BadgeAward;
import com.campusoverflow.reputation.domain.BadgeRepository;
import com.campusoverflow.reputation.domain.BadgeType;
import com.campusoverflow.shared.event.EventIds;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 徽章授予（幂等：已拥有则忽略）。 */
@Service
public class BadgeService {

    private final BadgeRepository badges;
    private final IntegrationEventPublisher events;
    private final Clock clock;

    public BadgeService(BadgeRepository badges, IntegrationEventPublisher events, Clock clock) {
        this.badges = badges;
        this.events = events;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void grant(long userId, BadgeType type) {
        if (badges.has(userId, type)) {
            return;
        }
        badges.award(new BadgeAward(userId, type, clock.instant()));
        events.publish(new BadgeAwardedEvent(EventIds.next(), clock.instant(), userId, type.name(),
                type.displayName()));
    }
}
