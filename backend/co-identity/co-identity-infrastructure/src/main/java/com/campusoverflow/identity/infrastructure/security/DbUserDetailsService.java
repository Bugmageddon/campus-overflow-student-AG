package com.campusoverflow.identity.infrastructure.security;

import com.campusoverflow.identity.domain.User;
import com.campusoverflow.identity.domain.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public DbUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        User u = users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        return new SecurityUser(u.id(), u.username(), u.passwordHash(), u.displayName(), u.role(), u.canLogin());
    }
}
