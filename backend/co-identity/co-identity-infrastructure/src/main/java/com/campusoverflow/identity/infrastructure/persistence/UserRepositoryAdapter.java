package com.campusoverflow.identity.infrastructure.persistence;

import com.campusoverflow.identity.domain.User;
import com.campusoverflow.identity.domain.UserRepository;
import com.campusoverflow.identity.domain.UserStatus;
import com.campusoverflow.identity.infrastructure.crypto.FieldEncryptor;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/** 仓储适配器：实现领域端口，负责领域对象与 JPA 实体之间的映射（手写映射器，见 ADR-008）。 */
@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;
    private final FieldEncryptor encryptor;

    public UserRepositoryAdapter(UserJpaRepository jpa, FieldEncryptor encryptor) {
        this.jpa = jpa;
        this.encryptor = encryptor;
    }

    @Override
    public Optional<User> findById(long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findByDisplayName(String displayName) {
        return jpa.findByDisplayName(displayName).map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    public boolean existsByDisplayName(String displayName) {
        return jpa.existsByDisplayName(displayName);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmailHash(encryptor.digest(email));
    }

    @Override
    public List<User> findAllById(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(this::toDomain).toList();
    }

    @Override
    public PageResult<User> findByVerified(boolean verified, PageRequest page) {
        Page<UserJpaEntity> p = jpa.findByVerified(verified,
                org.springframework.data.domain.PageRequest.of(page.page() - 1, page.size(),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResult<>(p.getContent().stream().map(this::toDomain).toList(), p.getTotalElements(),
                page.page(), page.size());
    }

    @Override
    public User save(User user) {
        UserJpaEntity e = user.id() == null
                ? new UserJpaEntity(user.username(), user.createdAt())
                : jpa.findById(user.id()).orElseThrow();
        e.setDisplayName(user.displayName());
        e.setEmailCipher(encryptor.encrypt(user.email()));
        e.setEmailHash(encryptor.digest(user.email()));
        e.setPasswordHash(user.passwordHash());
        e.setRole(user.role());
        e.setStatus(user.status().name());
        e.setVerified(user.verified());
        e.setCollege(user.college());
        e.setClassName(user.className());
        UserJpaEntity saved = jpa.save(e);
        if (user.id() == null) {
            user.assignId(saved.getId());
        }
        return user;
    }

    private User toDomain(UserJpaEntity e) {
        return User.reconstitute(e.getId(), e.getUsername(), e.getDisplayName(), encryptor.decrypt(e.getEmailCipher()),
                e.getPasswordHash(), e.getRole(), UserStatus.valueOf(e.getStatus()), e.isVerified(), e.getCollege(),
                e.getClassName(), e.getCreatedAt(), e.getVersion());
    }
}
