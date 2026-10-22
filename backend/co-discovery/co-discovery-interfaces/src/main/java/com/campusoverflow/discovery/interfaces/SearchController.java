package com.campusoverflow.discovery.interfaces;

import com.campusoverflow.discovery.application.DiscoveryQueryService;
import com.campusoverflow.discovery.application.RelatedQuestionView;
import com.campusoverflow.discovery.application.SearchResultView;
import com.campusoverflow.discovery.domain.search.SearchQuery;
import com.campusoverflow.discovery.domain.search.SearchSort;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "发现 Discovery")
@RestController
@RequestMapping("/api/v1")
public class SearchController {

    private final DiscoveryQueryService discovery;

    public SearchController(DiscoveryQueryService discovery) {
        this.discovery = discovery;
    }

    @Operation(summary = "搜索与筛选问题（全文检索支持中文；sort: relevance/newest/active/hot）")
    @GetMapping("/search/questions")
    public PageResult<SearchResultView> search(@RequestParam(required = false) String q,
                                               @RequestParam(required = false) Long courseId,
                                               @RequestParam(required = false) String tag,
                                               @RequestParam(required = false) String sort,
                                               @RequestParam(defaultValue = "false") boolean unanswered,
                                               @RequestParam(defaultValue = "false") boolean bounty,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        SearchSort s = sort == null || sort.isBlank() ? null : SearchSort.valueOf(sort.toUpperCase(Locale.ROOT));
        return discovery.search(new SearchQuery(q, courseId, tag, s, unanswered, bounty, new PageRequest(page, size)));
    }

    @Operation(summary = "相关问题（标签 Jaccard 相似度）")
    @GetMapping("/questions/{questionId}/related")
    public List<RelatedQuestionView> related(@PathVariable long questionId,
                                             @RequestParam(defaultValue = "5") int limit) {
        return discovery.related(questionId, limit);
    }
}
