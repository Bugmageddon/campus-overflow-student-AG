package com.campusoverflow.reputation.api.event;

import com.campusoverflow.shared.event.IntegrationEvent;
import java.time.Instant;

public record BountyOpenedEvent(String eventId, Instant occurredAt, long bountyId, long questionId, long sponsorId,
                                int points, Instant expiresAt) implements IntegrationEvent {
}
