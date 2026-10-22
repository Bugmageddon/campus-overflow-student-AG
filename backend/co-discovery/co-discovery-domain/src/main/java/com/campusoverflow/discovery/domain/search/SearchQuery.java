package com.campusoverflow.discovery.domain.search;

import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.page.PageRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 搜索条件（值对象）。关键词规范化：去除全文检索运算符，按空白切分为词项，最多 5 个。
 * MySQL ngram 分词粒度为 2（ngram_token_size=2），因此单个词项至少 2 个字符。
 */
public record SearchQuery(String keyword, Long courseId, String tag, SearchSort sort, boolean unansweredOnly,
                          boolean bountyOnly, PageRequest page) {

    private static final int MAX_TERMS = 5;

    public SearchQuery {
        keyword = keyword == null ? null : keyword.replaceAll("[\"+\\-<>()~*@]", " ").strip();
        if (keyword != null && keyword.isEmpty()) {
            keyword = null;
        }
        if (keyword != null && keyword.codePointCount(0, keyword.length()) > 50) {
            throw new BusinessRuleException("DIS-1002", "关键词不能超过 50 个字符");
        }
        if (keyword != null && terms(keyword).stream().anyMatch(t -> t.codePointCount(0, t.length()) < 2)) {
            throw new BusinessRuleException("DIS-1001", "每个关键词至少 2 个字符");
        }
        tag = tag == null || tag.isBlank() ? null : tag.strip().toLowerCase(Locale.ROOT);
        if (sort == null) {
            sort = keyword != null ? SearchSort.RELEVANCE : SearchSort.NEWEST;
        }
        if (sort == SearchSort.RELEVANCE && keyword == null) {
            sort = SearchSort.NEWEST;
        }
        page = page == null ? new PageRequest(1, 20) : page;
    }

    public List<String> terms() {
        // TODO(S3)：实现 SearchQuery.terms——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：SearchQuery.terms 尚未实现");
    }

    private static List<String> terms(String keyword) {
        // TODO(S3)：实现 SearchQuery.terms——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：SearchQuery.terms 尚未实现");
    }
}
