package com.campusoverflow.reputation.api.event;

import com.campusoverflow.shared.event.IntegrationEvent;
import java.time.Instant;

/** 悬赏到期未采纳（或被发起人自己的回答获得采纳）而关闭，冻结积分已退回。 */
public record BountyClosedEvent(String eventId, Instant occurredAt, long bountyId, long questionId, long sponsorId,
                                int refundedPoints) implements IntegrationEvent {
}
