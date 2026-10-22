package com.campusoverflow.identity.application;

import com.campusoverflow.identity.domain.Course;
import com.campusoverflow.identity.domain.CourseMember;
import com.campusoverflow.identity.domain.CourseRepository;
import com.campusoverflow.identity.domain.CourseRole;
import com.campusoverflow.identity.domain.User;
import com.campusoverflow.identity.domain.UserRepository;
import com.campusoverflow.shared.domain.ConflictException;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.security.Actor;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用例：课程与课程成员管理。 */
@Service
public class CourseService {

    private final CourseRepository courses;
    private final UserRepository users;
    private final Clock clock;

    public CourseService(CourseRepository courses, UserRepository users, Clock clock) {
        this.courses = courses;
        this.users = users;
        this.clock = clock;
    }

    @Transactional
    public long create(Actor actor, CreateCourseCommand cmd) {
        Course course = Course.create(actor, cmd.code(), cmd.name(), cmd.term(), clock.instant());
        if (courses.existsByCodeAndTerm(course.code(), course.term())) {
            throw new ConflictException("ID-2004", "该学期已存在同代码课程");
        }
        Course saved = courses.save(course);
        courses.saveMember(CourseMember.teacher(saved.id(), actor.userId(), clock.instant()));
        return saved.id();
    }

    @Transactional
    public void join(Actor actor, long courseId) {
        loadCourse(courseId);
        if (courses.findMember(courseId, actor.userId()).isPresent()) {
            throw new ConflictException("ID-2005", "你已加入该课程");
        }
        courses.saveMember(CourseMember.student(courseId, actor.userId(), clock.instant()));
    }

    /** 课程教师或管理员调整成员角色（例如任命助教）。 */
    @Transactional
    public void assignRole(Actor actor, long courseId, long userId, CourseRole role) {
        Course course = loadCourse(courseId);
        if (!actor.isAdmin() && course.teacherId() != actor.userId()) {
            throw new ForbiddenException("ID-3003", "只有课程教师或管理员可以调整成员角色");
        }
        CourseMember member = courses.findMember(courseId, userId)
                .orElseThrow(() -> new NotFoundException("ID-4003", "该用户不是课程成员"));
        member.changeRole(role);
        courses.saveMember(member);
    }

    @Transactional(readOnly = true)
    public List<CourseView> listAll(Actor actorOrNull) {
        List<Course> all = courses.findAll();
        Map<Long, CourseRole> mine = actorOrNull == null ? Map.of()
                : courses.findMembershipsOfUser(actorOrNull.userId()).stream()
                        .collect(Collectors.toMap(CourseMember::courseId, CourseMember::role));
        return toViews(all, mine);
    }

    @Transactional(readOnly = true)
    public List<CourseView> myCourses(long userId) {
        Map<Long, CourseRole> mine = courses.findMembershipsOfUser(userId).stream()
                .collect(Collectors.toMap(CourseMember::courseId, CourseMember::role));
        List<Course> list = courses.findAll().stream().filter(c -> mine.containsKey(c.id())).toList();
        return toViews(list, mine);
    }

    @Transactional(readOnly = true)
    public List<CourseMemberView> members(long courseId) {
        loadCourse(courseId);
        List<CourseMember> members = courses.findMembers(courseId);
        Map<Long, User> byId = users.findAllById(members.stream().map(CourseMember::userId).toList()).stream()
                .collect(Collectors.toMap(User::id, Function.identity()));
        return members.stream()
                .map(m -> new CourseMemberView(m.userId(),
                        byId.containsKey(m.userId()) ? byId.get(m.userId()).displayName() : "未知用户", m.role()))
                .toList();
    }

    private List<CourseView> toViews(List<Course> list, Map<Long, CourseRole> mine) {
        Map<Long, String> teacherNames = users.findAllById(list.stream().map(Course::teacherId).distinct().toList())
                .stream().collect(Collectors.toMap(User::id, User::displayName));
        return list.stream()
                .map(c -> new CourseView(c.id(), c.code(), c.name(), c.term(), c.teacherId(),
                        teacherNames.getOrDefault(c.teacherId(), "—"),
                        mine.containsKey(c.id()) ? mine.get(c.id()).name() : null))
                .toList();
    }

    private Course loadCourse(long courseId) {
        return courses.findById(courseId).orElseThrow(() -> new NotFoundException("ID-4002", "课程不存在"));
    }
}
