package com.campusoverflow.qa.application.command;

import java.util.List;

public record EditQuestionCommand(String title, String body, List<String> tags) {
}
