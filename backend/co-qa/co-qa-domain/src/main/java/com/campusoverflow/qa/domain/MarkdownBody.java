package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.Guard;

/** Markdown 正文。只保存原文；渲染与 XSS 净化在基础设施层完成。 */
public record MarkdownBody(String value) {

    public static MarkdownBody ofQuestion(String raw) {
        return new MarkdownBody(Guard.requireText(raw, 20, 30000, "QA-1002", "问题正文"));
    }

    public static MarkdownBody ofAnswer(String raw) {
        return new MarkdownBody(Guard.requireText(raw, 10, 30000, "QA-1002", "回答正文"));
    }
}
