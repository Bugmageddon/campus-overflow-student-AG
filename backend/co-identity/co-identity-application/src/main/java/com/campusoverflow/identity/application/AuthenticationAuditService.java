package com.campusoverflow.identity.application;

import com.campusoverflow.shared.audit.AuditTrail;
import com.campusoverflow.shared.util.Masking;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 登录审计：成功与失败都记录，账号脱敏。 */
@Service
public class AuthenticationAuditService {

    private final AuditTrail audit;

    public AuthenticationAuditService(AuditTrail audit) {
        this.audit = audit;
    }

    @Transactional
    public void loginSucceeded(long userId, String username) {
        audit.record("LOGIN_SUCCESS", userId, "USER", String.valueOf(userId), Masking.account(username));
    }

    @Transactional
    public void loginFailed(String username, String reason) {
        audit.record("LOGIN_FAILURE", null, "USER", null, Masking.account(username) + " " + reason);
    }
}
