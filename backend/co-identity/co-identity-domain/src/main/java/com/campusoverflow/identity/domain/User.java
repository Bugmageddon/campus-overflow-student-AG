package com.campusoverflow.identity.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.ConflictException;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.domain.Guard;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 用户聚合根（校园身份）。
 * <ul>
 *   <li>username：学号/工号，登录凭据，属于个人信息，不对外展示；</li>
 *   <li>displayName：公开昵称，全局唯一，用于 @提及；</li>
 *   <li>verified：是否已通过学院管理员的校园身份认证（显示认证徽章）。</li>
 * </ul>
 */
public class User {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9]{4,20}$");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Long id;
    private final String username;
    private final String displayName;
    private final String email;
    private final String passwordHash;
    private Role role;
    private UserStatus status;
    private boolean verified;
    private final String college;
    private final String className;
    private final Instant createdAt;
    private final long version;

    private User(Long id, String username, String displayName, String email, String passwordHash, Role role,
                 UserStatus status, boolean verified, String college, String className, Instant createdAt, long version) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.verified = verified;
        this.college = college;
        this.className = className;
        this.createdAt = createdAt;
        this.version = version;
    }

    /** 学生自助注册：角色固定为学生，待管理员完成校园身份认证。 */
    public static User register(String username, String displayName, String email, String passwordHash,
                                String college, String className, Instant now) {
        return new User(null, checkUsername(username), checkDisplayName(displayName), checkEmail(email),
                Objects.requireNonNull(passwordHash), Role.STUDENT, UserStatus.ACTIVE, false,
                optional(college, 50), optional(className, 50), now, 0);
    }

    /** 管理员创建教职工账号（教师/助教/管理员），创建即视为已认证。 */
    public static User createStaff(Actor admin, String username, String displayName, String email,
                                   String passwordHash, Role role, String college, Instant now) {
        requireAdmin(admin);
        if (role == Role.STUDENT) {
            throw new BusinessRuleException("ID-1005", "学生账号请通过注册流程创建");
        }
        return new User(null, checkUsername(username), checkDisplayName(displayName), checkEmail(email),
                Objects.requireNonNull(passwordHash), role, UserStatus.ACTIVE, true,
                optional(college, 50), null, now, 0);
    }

    /** 由持久化层重建聚合，不做业务校验。 */
    public static User reconstitute(Long id, String username, String displayName, String email, String passwordHash,
                                    Role role, UserStatus status, boolean verified, String college,
                                    String className, Instant createdAt, long version) {
        return new User(id, username, displayName, email, passwordHash, role, status, verified, college,
                className, createdAt, version);
    }

    /** 管理员完成校园身份认证。 */
    public void verify(Actor actor) {
        requireAdmin(actor);
        if (verified) {
            throw new ConflictException("ID-2006", "该用户已完成认证");
        }
        this.verified = true;
    }

    public void changeRole(Actor actor, Role newRole) {
        requireAdmin(actor);
        if (actor.is(requireId())) {
            throw new BusinessRuleException("ID-1006", "不能修改自己的角色");
        }
        this.role = Objects.requireNonNull(newRole);
    }

    public void disable(Actor actor) {
        requireAdmin(actor);
        if (actor.is(requireId())) {
            throw new BusinessRuleException("ID-1007", "不能禁用自己的账号");
        }
        this.status = UserStatus.DISABLED;
    }

    public boolean canLogin() {
        return status == UserStatus.ACTIVE;
    }

    private long requireId() {
        if (id == null) {
            throw new IllegalStateException("用户尚未持久化");
        }
        return id;
    }

    private static void requireAdmin(Actor actor) {
        if (actor == null || !actor.isAdmin()) {
            throw new ForbiddenException("ID-3001", "仅管理员可执行该操作");
        }
    }

    private static String checkUsername(String username) {
        if (username == null || !USERNAME.matcher(username).matches()) {
            throw new BusinessRuleException("ID-1001", "学号/工号应为 4–20 位字母或数字");
        }
        return username;
    }

    private static String checkDisplayName(String displayName) {
        String v = Guard.requireText(displayName, 2, 20, "ID-1002", "昵称");
        if (v.contains("@") || v.contains(" ")) {
            throw new BusinessRuleException("ID-1002", "昵称不能包含 @ 或空格");
        }
        return v;
    }

    private static String checkEmail(String email) {
        if (email == null || email.length() > 100 || !EMAIL.matcher(email).matches()) {
            throw new BusinessRuleException("ID-1003", "邮箱格式不正确");
        }
        return email.toLowerCase();
    }

    private static String optional(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.strip();
        return v.length() > max ? v.substring(0, max) : v;
    }

    /** 仅供持久化层在首次保存后回填主键。 */
    public void assignId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("ID 已分配");
        }
        this.id = id;
    }

    public Long id() { return id; }
    public String username() { return username; }
    public String displayName() { return displayName; }
    public String email() { return email; }
    public String passwordHash() { return passwordHash; }
    public Role role() { return role; }
    public UserStatus status() { return status; }
    public boolean verified() { return verified; }
    public String college() { return college; }
    public String className() { return className; }
    public Instant createdAt() { return createdAt; }
    public long version() { return version; }
}
