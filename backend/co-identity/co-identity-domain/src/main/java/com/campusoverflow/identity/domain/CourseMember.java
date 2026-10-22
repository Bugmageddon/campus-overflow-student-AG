package com.campusoverflow.identity.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;
import java.time.Instant;
import java.util.Objects;

/** 课程成员关系：授权判定（谁能在该课程中采纳、认证、悬赏）的依据。 */
public class CourseMember {

    private final long courseId;
    private final long userId;
    private CourseRole role;
    private final Instant joinedAt;

    private CourseMember(long courseId, long userId, CourseRole role, Instant joinedAt) {
        this.courseId = courseId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public static CourseMember student(long courseId, long userId, Instant now) {
        return new CourseMember(courseId, userId, CourseRole.STUDENT, now);
    }

    public static CourseMember teacher(long courseId, long userId, Instant now) {
        return new CourseMember(courseId, userId, CourseRole.TEACHER, now);
    }

    public static CourseMember reconstitute(long courseId, long userId, CourseRole role, Instant joinedAt) {
        return new CourseMember(courseId, userId, role, joinedAt);
    }

    /** 调整课程内角色（如任命助教）。课程教师本人的角色不可被降级。 */
    public void changeRole(CourseRole newRole) {
        if (role == CourseRole.TEACHER && newRole != CourseRole.TEACHER) {
            throw new BusinessRuleException("ID-1013", "不能修改课程教师的角色");
        }
        this.role = Objects.requireNonNull(newRole);
    }

    public long courseId() { return courseId; }
    public long userId() { return userId; }
    public CourseRole role() { return role; }
    public Instant joinedAt() { return joinedAt; }
}
