package com.campusoverflow.identity.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.domain.Guard;
import com.campusoverflow.shared.security.Actor;
import java.time.Instant;
import java.util.regex.Pattern;

/** 课程：课程专属问答区的载体。同一课程代码在同一学期唯一。 */
public class Course {

    private static final Pattern CODE = Pattern.compile("^[A-Za-z0-9-]{2,20}$");

    private Long id;
    private final String code;
    private final String name;
    private final String term;
    private final long teacherId;
    private final Instant createdAt;

    private Course(Long id, String code, String name, String term, long teacherId, Instant createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.term = term;
        this.teacherId = teacherId;
        this.createdAt = createdAt;
    }

    public static Course create(Actor actor, String code, String name, String term, Instant now) {
        if (actor == null || !actor.isTeacherOrAdmin()) {
            throw new ForbiddenException("ID-3002", "仅教师或管理员可以创建课程");
        }
        if (code == null || !CODE.matcher(code).matches()) {
            throw new BusinessRuleException("ID-1010", "课程代码应为 2–20 位字母、数字或连字符");
        }
        return new Course(null, code.toUpperCase(), Guard.requireText(name, 2, 50, "ID-1011", "课程名称"),
                Guard.requireText(term, 2, 20, "ID-1012", "学期"), actor.userId(), now);
    }

    public static Course reconstitute(Long id, String code, String name, String term, long teacherId, Instant createdAt) {
        return new Course(id, code, name, term, teacherId, createdAt);
    }

    public void assignId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("ID 已分配");
        }
        this.id = id;
    }

    public Long id() { return id; }
    public String code() { return code; }
    public String name() { return name; }
    public String term() { return term; }
    public long teacherId() { return teacherId; }
    public Instant createdAt() { return createdAt; }
}
