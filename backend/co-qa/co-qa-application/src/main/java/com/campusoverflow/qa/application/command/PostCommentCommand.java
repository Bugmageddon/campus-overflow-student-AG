package com.campusoverflow.qa.application.command;

import com.campusoverflow.qa.domain.TargetType;

public record PostCommentCommand(TargetType targetType, long targetId, Long parentId, String body) {
}
