package com.campusoverflow.shared.security;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发起当前操作的主体。由接口层从安全上下文中解析后显式传入应用服务，
 * 应用层与领域层因此不依赖 Spring Security，也便于单元测试构造任意身份。
 */
public record Actor(long userId, String displayName, Role role) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isTeacherOrAdmin() {
        return role == Role.TEACHER || role == Role.ADMIN;
    }

    public boolean is(long otherUserId) {
        return userId == otherUserId;
    }
}
