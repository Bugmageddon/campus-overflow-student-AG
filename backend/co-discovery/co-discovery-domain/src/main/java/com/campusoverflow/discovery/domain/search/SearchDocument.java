package com.campusoverflow.discovery.domain.search;

import java.time.Instant;
import java.util.List;

/** 搜索投影文档（与 Q&A 的问题聚合是两个模型：这里只关心“如何被找到”）。 */
public record SearchDocument(long questionId, Long courseId, long authorId, String title, String bodyText,
                             List<String> tags, Instant createdAt) {

    public SearchDocument {
        tags = List.copyOf(tags);
    }
}
