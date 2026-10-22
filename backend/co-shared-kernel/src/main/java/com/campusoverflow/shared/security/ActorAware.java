package com.campusoverflow.shared.security;

/** 由安全主体（Principal）实现，接口层据此把认证信息转换为 {@link Actor}。 */
public interface ActorAware {
    Actor toActor();
}
