package com.campusoverflow.identity.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;

/** 口令策略：8–64 位，至少包含字母与数字。 */
public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    public static void validate(String raw) {
        if (raw == null || raw.length() < 8 || raw.length() > 64) {
            throw new BusinessRuleException("ID-1004", "密码长度应为 8–64 位");
        }
        boolean letter = raw.chars().anyMatch(Character::isLetter);
        boolean digit = raw.chars().anyMatch(Character::isDigit);
        if (!letter || !digit) {
            throw new BusinessRuleException("ID-1004", "密码必须同时包含字母和数字");
        }
    }
}
