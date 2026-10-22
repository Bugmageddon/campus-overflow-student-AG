package com.campusoverflow.identity.application;

import com.campusoverflow.identity.api.CourseSummary;
import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.identity.domain.CourseRepository;
import com.campusoverflow.identity.domain.User;
import com.campusoverflow.identity.domain.UserRepository;
import com.campusoverflow.shared.security.Role;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 开放主机服务的实现：把领域对象翻译为发布语言（UserSummary / CourseSummary）。 */
@Service
@Transactional(readOnly = true)
public class IdentityApiImpl implements IdentityApi {

    private final UserRepository users;
    private final CourseRepository courses;

    public IdentityApiImpl(UserRepository users, CourseRepository courses) {
        this.users = users;
        this.courses = courses;
    }

    @Override
    public Optional<UserSummary> findUser(long userId) {
        return users.findById(userId).map(IdentityApiImpl::toSummary);
    }

    @Override
    public Map<Long, UserSummary> findUsers(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return users.findAllById(userIds.stream().distinct().toList()).stream()
                .map(IdentityApiImpl::toSummary)
                .collect(Collectors.toMap(UserSummary::id, Function.identity()));
    }

    @Override
    public Optional<UserSummary> findByDisplayName(String displayName) {
        return users.findByDisplayName(displayName).map(IdentityApiImpl::toSummary);
    }

    @Override
    public Optional<CourseSummary> findCourse(long courseId) {
        return courses.findById(courseId)
                .map(c -> new CourseSummary(c.id(), c.code(), c.name(), c.term(), c.teacherId()));
    }

    @Override
    public boolean canManageCourse(long userId, Long courseId) {
        Optional<User> user = users.findById(userId);
        if (user.isEmpty()) {
            return false;
        }
        Role role = user.get().role();
        if (role == Role.ADMIN) {
            return true;
        }
        if (courseId == null) {
            return role == Role.TEACHER;
        }
        return courses.findMember(courseId, userId).map(m -> m.role().canManage()).orElse(false);
    }

    private static UserSummary toSummary(User u) {
        return new UserSummary(u.id(), u.displayName(), u.role(), u.verified(), u.college());
    }
}
