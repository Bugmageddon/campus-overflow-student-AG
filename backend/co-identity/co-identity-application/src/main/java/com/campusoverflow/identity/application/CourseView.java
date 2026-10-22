package com.campusoverflow.identity.application;

public record CourseView(long id, String code, String name, String term, long teacherId, String teacherName,
                         String myRole) {
}
