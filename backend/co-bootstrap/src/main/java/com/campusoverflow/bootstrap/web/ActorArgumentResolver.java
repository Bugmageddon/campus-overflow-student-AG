package com.campusoverflow.bootstrap.web;

import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.ActorAware;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 把安全上下文中的主体解析为控制器参数：{@code Actor}（必须登录）或 {@code Optional<Actor>}（可匿名）。
 * 这样控制器与应用服务都不直接依赖 Spring Security API。
 */
public class ActorArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (parameter.getParameterType() == Actor.class) {
            return true;
        }
        if (parameter.getParameterType() == Optional.class) {
            Type type = parameter.getGenericParameterType();
            return type instanceof ParameterizedType pt && pt.getActualTypeArguments()[0] == Actor.class;
        }
        return false;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Actor actor = auth != null && auth.getPrincipal() instanceof ActorAware aware ? aware.toActor() : null;
        if (parameter.getParameterType() == Optional.class) {
            return Optional.ofNullable(actor);
        }
        if (actor == null) {
            throw new AuthenticationCredentialsNotFoundException("请先登录");
        }
        return actor;
    }
}
