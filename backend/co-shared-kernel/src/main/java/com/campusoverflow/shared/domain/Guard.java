package com.campusoverflow.shared.domain;

import java.util.Objects;

/** 领域层通用的前置条件检查工具。 */
public final class Guard {

    private Guard() {
    }

    /** 校验文本非空且长度（按 Unicode 码点计）在 [min, max] 内，返回去除首尾空白后的值。 */
    public static String requireText(String value, int min, int max, String code, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(code, fieldName + "不能为空");
        }
        String trimmed = value.strip();
        int len = trimmed.codePointCount(0, trimmed.length());
        if (len < min || len > max) {
            throw new BusinessRuleException(code,
                    fieldName + "长度应在 " + min + "–" + max + " 个字符之间（当前 " + len + "）");
        }
        return trimmed;
    }

    public static <T> T requireNonNull(T value, String code, String fieldName) {
        if (Objects.isNull(value)) {
            throw new BusinessRuleException(code, fieldName + "不能为空");
        }
        return value;
    }
}
