package com.campusoverflow.reputation.api.event;

import com.campusoverflow.shared.event.IntegrationEvent;
import java.time.Instant;

public record ReputationChangedEvent(String eventId, Instant occurredAt, long userId, int delta, int reputation,
                                     String reason) implements IntegrationEvent {
}
