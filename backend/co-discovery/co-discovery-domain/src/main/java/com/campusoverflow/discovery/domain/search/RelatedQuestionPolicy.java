package com.campusoverflow.discovery.domain.search;

import java.util.Comparator;
import java.util.List;

/**
 * 相关问题推荐（规则引擎，非 AI）：按标签集合的 Jaccard 相似度排序，
 * J(A,B) = |A∩B| / |A∪B|；相似度相同则得分高、已采纳者优先。
 */
public final class RelatedQuestionPolicy {

    private RelatedQuestionPolicy() {
    }

    public static double jaccard(int shared, int sizeA, int sizeB) {
        int union = sizeA + sizeB - shared;
        return union == 0 ? 0.0 : (double) shared / union;
    }

    public static List<SearchIndex.RelatedCandidate> rank(List<SearchIndex.RelatedCandidate> candidates, int ownTagCount,
                                                          int limit) {
        return candidates.stream()
                .sorted(Comparator
                        .comparingDouble((SearchIndex.RelatedCandidate c) ->
                                jaccard(c.sharedTags(), ownTagCount, c.tagCount())).reversed()
                        .thenComparing(Comparator.comparingInt(SearchIndex.RelatedCandidate::score).reversed())
                        .thenComparing(c -> !c.accepted()))
                .limit(limit)
                .toList();
    }
}
