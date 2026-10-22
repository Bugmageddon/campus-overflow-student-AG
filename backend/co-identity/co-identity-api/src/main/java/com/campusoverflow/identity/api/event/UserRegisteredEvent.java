package com.campusoverflow.identity.api.event;

import com.campusoverflow.shared.event.IntegrationEvent;
import com.campusoverflow.shared.security.Role;
import java.time.Instant;

/** 新用户注册（含管理员创建教职工账号）。Reputation 订阅以开立声誉账户。 */
public record UserRegisteredEvent(String eventId, Instant occurredAt, long userId, String displayName, Role role)
        implements IntegrationEvent {
}
