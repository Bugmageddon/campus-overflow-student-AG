package com.campusoverflow.identity.domain;

import java.util.List;
import java.util.Optional;

public interface CourseRepository {
    Course save(Course course);

    Optional<Course> findById(long id);

    List<Course> findAll();

    boolean existsByCodeAndTerm(String code, String term);

    Optional<CourseMember> findMember(long courseId, long userId);

    CourseMember saveMember(CourseMember member);

    List<CourseMember> findMembers(long courseId);

    List<CourseMember> findMembershipsOfUser(long userId);
}
