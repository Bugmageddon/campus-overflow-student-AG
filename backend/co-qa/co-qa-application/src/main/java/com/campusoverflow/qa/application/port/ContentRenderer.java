package com.campusoverflow.qa.application.port;

/** Markdown 渲染与净化端口（实现：commonmark + OWASP HTML Sanitizer）。质量场景 QS-07。 */
public interface ContentRenderer {
    /** 渲染为经过白名单净化的安全 HTML。 */
    String toSafeHtml(String markdown);

    /** 提取纯文本摘要（用于列表摘要与搜索索引）。 */
    String toPlainText(String markdown, int maxLength);
}
