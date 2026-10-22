package com.campusoverflow.qa.infrastructure.render;

import com.campusoverflow.qa.application.port.ContentRenderer;
import java.util.List;
import java.util.regex.Pattern;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Component;

/**
 * 两道防线防 XSS（QS-07）：
 * ① commonmark 转义 Markdown 中的原始 HTML、清洗危险 URL；
 * ② OWASP Java HTML Sanitizer 白名单兜底（前端展示时还会再经 DOMPurify 处理）。
 */
@Component
public class CommonmarkContentRenderer implements ContentRenderer {

    private static final List<Extension> EXTENSIONS = List.of(TablesExtension.create());

    private static final PolicyFactory POLICY = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.LINKS)
            .and(Sanitizers.TABLES)
            .and(Sanitizers.IMAGES)
            .and(new HtmlPolicyBuilder()
                    .allowElements("pre", "code", "hr", "br", "del")
                    .allowAttributes("class").matching(Pattern.compile("language-[\\w+#-]{1,30}")).onElements("code")
                    .toFactory());

    private final Parser parser = Parser.builder().extensions(EXTENSIONS).build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(EXTENSIONS)
            .escapeHtml(true).sanitizeUrls(true).build();
    private final TextContentRenderer textRenderer = TextContentRenderer.builder().build();

    @Override
    public String toSafeHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        Node doc = parser.parse(markdown);
        return POLICY.sanitize(htmlRenderer.render(doc));
    }

    @Override
    public String toPlainText(String markdown, int maxLength) {
        if (markdown == null) {
            return "";
        }
        String text = textRenderer.render(parser.parse(markdown)).replaceAll("\\s+", " ").strip();
        if (text.codePointCount(0, text.length()) <= maxLength) {
            return text;
        }
        return text.substring(0, text.offsetByCodePoints(0, maxLength)) + "…";
    }
}
