package com.campusoverflow.identity.domain;

import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 用户仓储端口（依赖倒置：接口在领域层，实现在基础设施层）。 */
public interface UserRepository {
    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByDisplayName(String displayName);

    boolean existsByUsername(String username);

    boolean existsByDisplayName(String displayName);

    boolean existsByEmail(String email);

    List<User> findAllById(Collection<Long> ids);

    PageResult<User> findByVerified(boolean verified, PageRequest page);

    User save(User user);
}
