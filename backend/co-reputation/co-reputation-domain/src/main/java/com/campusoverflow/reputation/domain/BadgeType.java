package com.campusoverflow.reputation.domain;

public enum BadgeType {
    FIRST_ANSWER("初来乍到", "提交第一个回答"),
    FIRST_ACCEPTED("初露锋芒", "第一个回答被采纳"),
    HELPER_10("解惑达人", "累计 10 个回答被采纳"),
    TEACHER_ENDORSED("名师认可", "回答获得教师认证"),
    REPUTATION_100("小有名气", "声誉达到 100"),
    REPUTATION_1000("德高望重", "声誉达到 1000");

    private final String displayName;
    private final String description;

    BadgeType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }
}
