package com.campusoverflow.identity.application;

import com.campusoverflow.identity.domain.CourseRole;

public record CourseMemberView(long userId, String displayName, CourseRole role) {
}
