package com.campusoverflow.identity.application;

import com.campusoverflow.shared.security.Role;
import java.util.List;

/** 当前登录用户的完整资料（仅本人可见）。 */
public record MeView(long id, String username, String displayName, String email, Role role, boolean verified,
                     String college, String className, List<CourseView> courses) {
}
