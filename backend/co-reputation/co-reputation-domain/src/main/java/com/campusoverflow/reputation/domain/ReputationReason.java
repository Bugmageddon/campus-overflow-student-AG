package com.campusoverflow.reputation.domain;

public enum ReputationReason {
    ANSWER_ACCEPTED("回答被采纳"),
    QUESTION_VOTE("问题被投票"),
    ANSWER_VOTE("回答被投票"),
    BOUNTY_AWARDED("赢得悬赏");

    private final String label;

    ReputationReason(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isVote() {
        return this == QUESTION_VOTE || this == ANSWER_VOTE;
    }
}
