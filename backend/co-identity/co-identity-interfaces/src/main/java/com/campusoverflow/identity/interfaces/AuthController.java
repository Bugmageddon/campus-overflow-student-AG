package com.campusoverflow.identity.interfaces;

import com.campusoverflow.identity.application.AuthenticationAuditService;
import com.campusoverflow.identity.application.LoginThrottle;
import com.campusoverflow.identity.application.MeView;
import com.campusoverflow.identity.application.RegisterUserCommand;
import com.campusoverflow.identity.application.UserQueryService;
import com.campusoverflow.identity.application.UserRegistrationService;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.ActorAware;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。采用服务端会话（Spring Session + Redis）+ HttpOnly Cookie，而非长效 JWT（见 ADR-003）。
 * 登出由安全过滤链处理：POST /api/v1/auth/logout。
 */
@Tag(name = "认证 Auth")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRegistrationService registration;
    private final UserQueryService userQuery;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final LoginThrottle throttle;
    private final AuthenticationAuditService authAudit;

    public AuthController(UserRegistrationService registration, UserQueryService userQuery,
                          AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository, LoginThrottle throttle,
                          AuthenticationAuditService authAudit) {
        this.registration = registration;
        this.userQuery = userQuery;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.throttle = throttle;
        this.authAudit = authAudit;
    }

    @Operation(summary = "获取 CSRF Token（同时写入 XSRF-TOKEN Cookie）")
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @Operation(summary = "学生注册")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Long>> register(@Valid @RequestBody RegisterRequest req) {
        long id = registration.register(new RegisterUserCommand(req.username(), req.displayName(), req.email(),
                req.password(), req.college(), req.className()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @Operation(summary = "登录（学号/工号 + 密码）")
    @PostMapping("/login")
    public MeView login(@Valid @RequestBody LoginRequest req, HttpServletRequest request,
                        HttpServletResponse response) {
        if (throttle.isBlocked(req.username())) {
            authAudit.loginFailed(req.username(), "THROTTLED");
            throw new LockedException("登录失败次数过多，请 1 分钟后再试");
        }
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(req.username(), req.password()));
        } catch (AuthenticationException e) {
            throttle.recordFailure(req.username());
            authAudit.loginFailed(req.username(), e.getClass().getSimpleName());
            throw new BadCredentialsException("账号或密码错误");
        }
        throttle.reset(req.username());
        // 防御会话固定攻击：认证成功后轮换会话 ID
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        Actor actor = ((ActorAware) auth.getPrincipal()).toActor();
        authAudit.loginSucceeded(actor.userId(), req.username());
        return userQuery.me(actor.userId());
    }

    @Operation(summary = "当前登录用户")
    @GetMapping("/me")
    public MeView me(@Parameter(hidden = true) Actor actor) {
        return userQuery.me(actor.userId());
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 4, max = 20) String username,
            @NotBlank @Size(min = 2, max = 20) String displayName,
            @NotBlank @Size(max = 100) String email,
            @NotBlank @Size(min = 8, max = 64) String password,
            @Size(max = 50) String college,
            @Size(max = 50) String className) {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }
}
