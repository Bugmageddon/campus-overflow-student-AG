package com.campusoverflow.identity.application;

import com.campusoverflow.identity.domain.User;
import com.campusoverflow.identity.domain.UserRepository;
import com.campusoverflow.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserQueryService {

    private final UserRepository users;
    private final CourseService courseService;

    public UserQueryService(UserRepository users, CourseService courseService) {
        this.users = users;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public MeView me(long userId) {
        User u = users.findById(userId).orElseThrow(() -> new NotFoundException("ID-4001", "用户不存在"));
        return new MeView(u.id(), u.username(), u.displayName(), u.email(), u.role(), u.verified(), u.college(),
                u.className(), courseService.myCourses(userId));
    }
}
