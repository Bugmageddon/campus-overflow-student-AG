package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.Guard;

/** Markdown 正文。只保存原文；渲染与 XSS 净化在基础设施层完成。 */
public record MarkdownBody(String value) {

    public static MarkdownBody ofQuestion(String raw) {
        // TODO(S2)：实现 MarkdownBody.ofQuestion——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：MarkdownBody.ofQuestion 尚未实现");
    }

    public static MarkdownBody ofAnswer(String raw) {
        // TODO(S2)：实现 MarkdownBody.ofAnswer——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：MarkdownBody.ofAnswer 尚未实现");
    }
}
