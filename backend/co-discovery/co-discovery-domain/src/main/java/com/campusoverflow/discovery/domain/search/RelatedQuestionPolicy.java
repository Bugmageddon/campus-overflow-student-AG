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
        // TODO(S3)：实现 RelatedQuestionPolicy.jaccard——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：RelatedQuestionPolicy.jaccard 尚未实现");
    }

    public static List<SearchIndex.RelatedCandidate> rank(List<SearchIndex.RelatedCandidate> candidates, int ownTagCount,
                                                          int limit) {
        // TODO(S3)：实现 RelatedQuestionPolicy.rank——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：RelatedQuestionPolicy.rank 尚未实现");
    }
}
