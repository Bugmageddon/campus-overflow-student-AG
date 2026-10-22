package com.campusoverflow.discovery.application;

import java.time.Instant;
import java.util.List;

public record SearchResultView(long id, String title, String excerpt, List<String> tags, Long courseId,
                               Author author, int score, int answerCount, boolean accepted, boolean bountyOpen,
                               Instant createdAt) {

    public record Author(long id, String displayName, String role, boolean verified) {
    }
}
