package com.campusoverflow.identity.api;

import com.campusoverflow.shared.security.Role;

/** 对外公开的用户摘要。注意：不包含学号/工号、邮箱等个人信息。 */
public record UserSummary(long id, String displayName, Role role, boolean verified, String college) {
}
