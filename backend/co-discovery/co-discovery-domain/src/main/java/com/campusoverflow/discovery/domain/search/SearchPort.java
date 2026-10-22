package com.campusoverflow.discovery.domain.search;

import com.campusoverflow.shared.page.PageResult;

/**
 * 搜索端口（ADR-005）。当前实现：MySQL FULLTEXT + ngram；替换为 Elasticsearch 时只需新增一个适配器，
 * 领域与应用层零改动——这正是“可演进性”这一驱动特征的落点。
 */
public interface SearchPort {
    PageResult<SearchHit> search(SearchQuery query);
}
