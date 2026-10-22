package com.campusoverflow.identity.application;

/** 登录防暴力破解端口（实现：Redis 计数器）。质量场景 QS-08。 */
public interface LoginThrottle {
    boolean isBlocked(String username);

    void recordFailure(String username);

    void reset(String username);
}
