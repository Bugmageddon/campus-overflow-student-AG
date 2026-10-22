package com.campusoverflow.shared.util;

/** 个人信息脱敏工具（日志与审计使用），满足《个人信息保护法》最小化原则。 */
public final class Masking {

    private Masking() {
    }

    /** 学号/工号：保留前 4 位与后 2 位，例如 2023001217 → 2023****17。 */
    public static String account(String value) {
        if (value == null || value.length() <= 6) {
            return "****";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 2);
    }

    /** 邮箱：保留首字符与域名，例如 alice@campus.edu → a***@campus.edu。 */
    public static String email(String value) {
        if (value == null || !value.contains("@")) {
            return "***";
        }
        int at = value.indexOf('@');
        return value.charAt(0) + "***" + value.substring(at);
    }
}
