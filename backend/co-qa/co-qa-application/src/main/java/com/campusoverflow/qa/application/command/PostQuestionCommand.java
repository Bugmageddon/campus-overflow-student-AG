package com.campusoverflow.qa.application.command;

import java.util.List;

public record PostQuestionCommand(Long courseId, String title, String body, List<String> tags) {
}
