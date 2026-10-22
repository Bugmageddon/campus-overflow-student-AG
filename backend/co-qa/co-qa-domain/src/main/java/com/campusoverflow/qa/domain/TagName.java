package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;
import java.util.Locale;
import java.util.regex.Pattern;

/** 标签名：统一小写，1–30 个字符，支持中文与 # + . - _（如 c++、c#、数据结构、2025秋）。 */
public record TagName(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[\\p{L}\\p{N}][\\p{L}\\p{N}#+.\\-_]{0,29}$");

    public TagName {
        if (value == null) {
            throw new BusinessRuleException("QA-1004", "标签不能为空");
        }
        value = value.strip().replaceFirst("^#", "").toLowerCase(Locale.ROOT);
        if (!PATTERN.matcher(value).matches()) {
            throw new BusinessRuleException("QA-1004", "标签格式不正确：" + value);
        }
    }
}
