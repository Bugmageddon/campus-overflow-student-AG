package com.campusoverflow.identity.application;

import com.campusoverflow.identity.api.event.UserRegisteredEvent;
import com.campusoverflow.identity.domain.PasswordHasher;
import com.campusoverflow.identity.domain.PasswordPolicy;
import com.campusoverflow.identity.domain.User;
import com.campusoverflow.identity.domain.UserRepository;
import com.campusoverflow.shared.audit.AuditTrail;
import com.campusoverflow.shared.domain.ConflictException;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.event.EventIds;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import com.campusoverflow.shared.util.Masking;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用例：管理员的用户管理（校园身份认证、创建教职工、调整角色）。 */
@Service
public class AdminUserService {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final IntegrationEventPublisher events;
    private final AuditTrail audit;
    private final Clock clock;

    public AdminUserService(UserRepository users, PasswordHasher hasher, IntegrationEventPublisher events,
                            AuditTrail audit, Clock clock) {
        this.users = users;
        this.hasher = hasher;
        this.events = events;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResult<UserView> listByVerification(Actor actor, boolean verified, PageRequest page) {
        requireAdmin(actor);
        return users.findByVerified(verified, page).map(AdminUserService::toView);
    }

    @Transactional
    public void verify(Actor actor, long userId) {
        User user = load(userId);
        user.verify(actor);
        users.save(user);
        audit.record("USER_VERIFIED", actor.userId(), "USER", String.valueOf(userId), null);
    }

    @Transactional
    public long createStaff(Actor actor, CreateStaffCommand cmd) {
        PasswordPolicy.validate(cmd.password());
        User user = User.createStaff(actor, cmd.username(), cmd.displayName(), cmd.email(),
                hasher.hash(cmd.password()), cmd.role(), cmd.college(), clock.instant());
        if (users.existsByUsername(user.username()) || users.existsByDisplayName(user.displayName())
                || users.existsByEmail(user.email())) {
            throw new ConflictException("ID-2001", "工号、昵称或邮箱已被使用");
        }
        User saved = users.save(user);
        events.publish(new UserRegisteredEvent(EventIds.next(), clock.instant(), saved.id(), saved.displayName(),
                saved.role()));
        audit.record("STAFF_CREATED", actor.userId(), "USER", String.valueOf(saved.id()),
                saved.role() + " " + Masking.account(saved.username()));
        return saved.id();
    }

    @Transactional
    public void changeRole(Actor actor, long userId, Role role) {
        User user = load(userId);
        Role old = user.role();
        user.changeRole(actor, role);
        users.save(user);
        audit.record("ROLE_CHANGED", actor.userId(), "USER", String.valueOf(userId), old + " -> " + role);
    }

    private User load(long userId) {
        return users.findById(userId).orElseThrow(() -> new NotFoundException("ID-4001", "用户不存在"));
    }

    private static void requireAdmin(Actor actor) {
        if (!actor.isAdmin()) {
            throw new ForbiddenException("ID-3001", "仅管理员可执行该操作");
        }
    }

    static UserView toView(User u) {
        return new UserView(u.id(), Masking.account(u.username()), u.displayName(), u.role(), u.verified(),
                u.college(), u.className(), u.createdAt());
    }
}
