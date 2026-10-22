package com.campusoverflow.qa.application.query;

import com.campusoverflow.identity.api.UserSummary;

public record AuthorView(long id, String displayName, String role, boolean verified) {

    public static AuthorView of(long id, UserSummary summary) {
        if (summary == null) {
            return new AuthorView(id, "已注销用户", "STUDENT", false);
        }
        return new AuthorView(id, summary.displayName(), summary.role().name(), summary.verified());
    }
}
