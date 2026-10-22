package com.campusoverflow.qa.interfaces;

import com.campusoverflow.qa.application.command.AnswerCommandService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "回答 Answer")
@RestController
@RequestMapping("/api/v1")
public class AnswerController {

    private final AnswerCommandService answers;

    public AnswerController(AnswerCommandService answers) {
        this.answers = answers;
    }

    @Operation(summary = "提交回答")
    @PostMapping("/questions/{questionId}/answers")
    public ResponseEntity<Map<String, Long>> submit(@Parameter(hidden = true) Actor actor,
                                                    @PathVariable long questionId,
                                                    @Valid @RequestBody AnswerRequest req) {
        long id = answers.submit(actor, questionId, req.body());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @Operation(summary = "编辑回答")
    @PutMapping("/answers/{answerId}")
    public ResponseEntity<Void> edit(@Parameter(hidden = true) Actor actor, @PathVariable long answerId,
                                     @Valid @RequestBody AnswerRequest req) {
        answers.edit(actor, answerId, req.body());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "删除回答（软删除）")
    @DeleteMapping("/answers/{answerId}")
    public ResponseEntity<Void> delete(@Parameter(hidden = true) Actor actor, @PathVariable long answerId) {
        answers.delete(actor, answerId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "采纳最佳答案（提问者或课程教师/助教）")
    @PutMapping("/questions/{questionId}/accepted-answer")
    public ResponseEntity<Void> accept(@Parameter(hidden = true) Actor actor, @PathVariable long questionId,
                                       @Valid @RequestBody AcceptRequest req) {
        answers.accept(actor, questionId, req.answerId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "教师认证回答")
    @PostMapping("/answers/{answerId}/endorsement")
    public ResponseEntity<Void> endorse(@Parameter(hidden = true) Actor actor, @PathVariable long answerId) {
        answers.endorse(actor, answerId);
        return ResponseEntity.noContent().build();
    }

    public record AnswerRequest(@NotBlank @Size(max = 30000) String body) {
    }

    public record AcceptRequest(@NotNull Long answerId) {
    }
}
