package com.campusoverflow.shared.security;

/**
 * 全局角色（RBAC）。课程内的细粒度角色（某门课的助教/教师）由 Identity 上下文的课程成员关系表达。
 */
public enum Role {
    STUDENT("学生"),
    TA("助教"),
    TEACHER("教师"),
    ADMIN("管理员");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
