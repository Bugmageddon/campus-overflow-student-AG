package com.campusoverflow.identity.domain;

/** 口令哈希端口（实现：基础设施层 BCrypt）。领域层不依赖具体算法。 */
public interface PasswordHasher {
    String hash(String raw);

    boolean matches(String raw, String hash);
}
