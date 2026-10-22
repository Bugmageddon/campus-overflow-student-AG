package com.campusoverflow.discovery.domain.search;

import java.time.Instant;
import java.util.List;

public record SearchHit(long questionId, String title, String snippet, List<String> tags, Long courseId,
                        long authorId, int score, int answerCount, boolean accepted, boolean bountyOpen,
                        Instant createdAt) {
}
