package com.campusoverflow.qa.interfaces;

import com.campusoverflow.qa.application.command.CommentService;
import com.campusoverflow.qa.application.command.PostCommentCommand;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.shared.security.Actor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "评论 Comment")
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService comments;

    public CommentController(CommentService comments) {
        this.comments = comments;
    }

    @Operation(summary = "发表评论或二级回复（支持 @昵称 提醒）")
    @PostMapping
    public ResponseEntity<Map<String, Long>> post(@Parameter(hidden = true) Actor actor,
                                                  @Valid @RequestBody CommentRequest req) {
        long id = comments.post(actor, new PostCommentCommand(req.targetType(), req.targetId(), req.parentId(),
                req.body()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    public record CommentRequest(@NotNull TargetType targetType, @NotNull Long targetId, Long parentId,
                                 @NotBlank @Size(max = 600) String body) {
    }
}
