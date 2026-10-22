package com.campusoverflow.reputation.application;

public record LeaderboardEntryView(int rank, long userId, String displayName, boolean verified, long score) {
}
