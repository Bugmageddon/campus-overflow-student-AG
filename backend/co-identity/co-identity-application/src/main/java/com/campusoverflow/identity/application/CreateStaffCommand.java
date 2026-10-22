package com.campusoverflow.identity.application;

import com.campusoverflow.shared.security.Role;

public record CreateStaffCommand(String username, String displayName, String email, String password,
                                 Role role, String college) {
}
