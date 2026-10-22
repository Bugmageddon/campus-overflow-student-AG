package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.Guard;

/** 问题标题：10–150 个字符。 */
public record Title(String value) {
    public Title {
        value = Guard.requireText(value, 10, 150, "QA-1001", "标题");
    }
}
