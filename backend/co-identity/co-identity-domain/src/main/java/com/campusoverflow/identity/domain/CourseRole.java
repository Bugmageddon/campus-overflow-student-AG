package com.campusoverflow.identity.domain;

/** 课程内角色。 */
public enum CourseRole {
    STUDENT,
    TA,
    TEACHER;

    public boolean canManage() {
        return this == TA || this == TEACHER;
    }
}
