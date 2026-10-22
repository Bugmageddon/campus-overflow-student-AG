package com.campusoverflow.discovery.application;

import com.campusoverflow.discovery.domain.search.RelatedQuestionPolicy;
import com.campusoverflow.discovery.domain.search.SearchHit;
import com.campusoverflow.discovery.domain.search.SearchIndex;
import com.campusoverflow.discovery.domain.search.SearchPort;
import com.campusoverflow.discovery.domain.search.SearchQuery;
import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.shared.page.PageResult;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用例：搜索与筛选、相关问题推荐。 */
@Service
@Transactional(readOnly = true)
public class DiscoveryQueryService {

    private final SearchPort search;
    private final SearchIndex index;
    private final IdentityApi identity;

    public DiscoveryQueryService(SearchPort search, SearchIndex index, IdentityApi identity) {
        this.search = search;
        this.index = index;
        this.identity = identity;
    }

    public PageResult<SearchResultView> search(SearchQuery query) {
        // TODO(S3)：实现 DiscoveryQueryService.search——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：DiscoveryQueryService.search 尚未实现");
    }

    public List<RelatedQuestionView> related(long questionId, int limit) {
        // TODO(S3)：实现 DiscoveryQueryService.related——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：DiscoveryQueryService.related 尚未实现");
    }
}
