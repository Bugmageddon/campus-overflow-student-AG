package com.campusoverflow.discovery.application;

public record RelatedQuestionView(long id, String title, int answerCount, boolean accepted, double similarity) {
}
