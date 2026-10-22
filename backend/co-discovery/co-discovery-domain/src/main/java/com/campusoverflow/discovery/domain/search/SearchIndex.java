package com.campusoverflow.discovery.domain.search;

import java.time.Instant;
import java.util.List;

/** 搜索投影的写端（由事件投影器维护）。 */
public interface SearchIndex {

    void insert(SearchDocument document);

    void updateText(long questionId, String title, String bodyText, List<String> tags, Instant at);

    void adjustAnswerCount(long questionId, int delta, Instant at);

    void adjustScore(long questionId, int delta);

    void markAccepted(long questionId, Instant at);

    void setBountyOpen(long questionId, boolean open);

    void markDeleted(long questionId);

    List<String> findTags(long questionId);

    /** 与给定标签至少共享一个标签的候选问题（排除自身与已删除）。 */
    List<RelatedCandidate> findCandidatesSharingTags(long questionId, List<String> tags, int limit);

    record RelatedCandidate(long questionId, String title, int sharedTags, int tagCount, int score,
                            int answerCount, boolean accepted) {
    }
}
