package com.campusoverflow.reputation.api.event;

import com.campusoverflow.shared.event.IntegrationEvent;
import java.time.Instant;

public record BadgeAwardedEvent(String eventId, Instant occurredAt, long userId, String badgeCode, String badgeName)
        implements IntegrationEvent {
}
