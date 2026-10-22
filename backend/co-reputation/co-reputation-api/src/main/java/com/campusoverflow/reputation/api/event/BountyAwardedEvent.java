package com.campusoverflow.reputation.api.event;

import com.campusoverflow.shared.event.IntegrationEvent;
import java.time.Instant;

public record BountyAwardedEvent(String eventId, Instant occurredAt, long bountyId, long questionId, long sponsorId,
                                 long winnerId, int points) implements IntegrationEvent {
}
