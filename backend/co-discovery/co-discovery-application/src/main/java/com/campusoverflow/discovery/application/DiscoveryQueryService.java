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
        PageResult<SearchHit> result = search.search(query);
        Map<Long, UserSummary> authors = identity.findUsers(
                result.items().stream().map(SearchHit::authorId).distinct().toList());
        List<SearchResultView> items = result.items().stream().map(hit -> {
            UserSummary author = authors.get(hit.authorId());
            return new SearchResultView(hit.questionId(), hit.title(), hit.snippet(), hit.tags(), hit.courseId(),
                    author == null
                            ? new SearchResultView.Author(hit.authorId(), "某同学", "STUDENT", false)
                            : new SearchResultView.Author(author.id(), author.displayName(), author.role().name(),
                                    author.verified()),
                    hit.score(), hit.answerCount(), hit.accepted(), hit.bountyOpen(), hit.createdAt());
        }).toList();
        return new PageResult<>(items, result.total(), result.page(), result.size());
    }

    public List<RelatedQuestionView> related(long questionId, int limit) {
        List<String> tags = index.findTags(questionId);
        if (tags.isEmpty()) {
            return List.of();
        }
        return RelatedQuestionPolicy
                .rank(index.findCandidatesSharingTags(questionId, tags, 50), tags.size(), limit)
                .stream()
                .map(c -> new RelatedQuestionView(c.questionId(), c.title(), c.answerCount(), c.accepted(),
                        RelatedQuestionPolicy.jaccard(c.sharedTags(), tags.size(), c.tagCount())))
                .toList();
    }
}
