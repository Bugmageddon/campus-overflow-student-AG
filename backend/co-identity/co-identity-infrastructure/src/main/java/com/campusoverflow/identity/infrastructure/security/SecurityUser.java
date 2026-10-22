package com.campusoverflow.identity.infrastructure.security;

import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.ActorAware;
import com.campusoverflow.shared.security.Role;
import java.io.Serial;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 存放在会话（Redis）中的安全主体。只保存授权所需的最少信息；认证成功后口令哈希会被擦除。
 */
public class SecurityUser implements UserDetails, CredentialsContainer, ActorAware {

    @Serial
    private static final long serialVersionUID = 1L;

    private final long userId;
    private final String username;
    private String passwordHash;
    private final String displayName;
    private final Role role;
    private final boolean enabled;

    public SecurityUser(long userId, String username, String passwordHash, String displayName, Role role,
                        boolean enabled) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
        this.enabled = enabled;
    }

    @Override
    public Actor toActor() {
        return new Actor(userId, displayName, role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }

    public long getUserId() {
        return userId;
    }
}
