package com.campusoverflow.identity.application;

import com.campusoverflow.identity.api.event.UserRegisteredEvent;
import com.campusoverflow.identity.domain.PasswordHasher;
import com.campusoverflow.identity.domain.PasswordPolicy;
import com.campusoverflow.identity.domain.User;
import com.campusoverflow.identity.domain.UserRepository;
import com.campusoverflow.shared.audit.AuditTrail;
import com.campusoverflow.shared.domain.ConflictException;
import com.campusoverflow.shared.event.EventIds;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.util.Masking;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用例：学生自助注册。 */
@Service
public class UserRegistrationService {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final IntegrationEventPublisher events;
    private final AuditTrail audit;
    private final Clock clock;

    public UserRegistrationService(UserRepository users, PasswordHasher hasher, IntegrationEventPublisher events,
                                   AuditTrail audit, Clock clock) {
        this.users = users;
        this.hasher = hasher;
        this.events = events;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public long register(RegisterUserCommand cmd) {
        PasswordPolicy.validate(cmd.password());
        User user = User.register(cmd.username(), cmd.displayName(), cmd.email(), hasher.hash(cmd.password()),
                cmd.college(), cmd.className(), clock.instant());
        ensureUnique(user);
        User saved = users.save(user);
        events.publish(new UserRegisteredEvent(EventIds.next(), clock.instant(), saved.id(), saved.displayName(),
                saved.role()));
        audit.record("USER_REGISTERED", saved.id(), "USER", String.valueOf(saved.id()),
                Masking.account(saved.username()));
        return saved.id();
    }

    void ensureUnique(User user) {
        if (users.existsByUsername(user.username())) {
            throw new ConflictException("ID-2001", "该学号/工号已注册");
        }
        if (users.existsByDisplayName(user.displayName())) {
            throw new ConflictException("ID-2002", "该昵称已被使用");
        }
        if (users.existsByEmail(user.email())) {
            throw new ConflictException("ID-2003", "该邮箱已被使用");
        }
    }
}
