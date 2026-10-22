package com.campusoverflow.qa.interfaces;

import com.campusoverflow.qa.application.command.EditQuestionCommand;
import com.campusoverflow.qa.application.command.PostQuestionCommand;
import com.campusoverflow.qa.application.command.QuestionCommandService;
import com.campusoverflow.qa.application.query.QuestionDetailView;
import com.campusoverflow.qa.application.query.QuestionQueryService;
import com.campusoverflow.qa.application.query.QuestionSummaryView;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "问题 Question")
@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionCommandService commands;
    private final QuestionQueryService queries;

    public QuestionController(QuestionCommandService commands, QuestionQueryService queries) {
        this.commands = commands;
        this.queries = queries;
    }

    @Operation(summary = "问题列表（最新优先，可按课程过滤）")
    @GetMapping
    public PageResult<QuestionSummaryView> list(@RequestParam(required = false) Long courseId,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return queries.list(courseId, new PageRequest(page, size));
    }

    @Operation(summary = "发布问题")
    @PostMapping
    public ResponseEntity<Map<String, Long>> post(@Parameter(hidden = true) Actor actor,
                                                  @Valid @RequestBody QuestionRequest req) {
        long id = commands.post(actor, new PostQuestionCommand(req.courseId(), req.title(), req.body(), req.tags()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @Operation(summary = "问题详情（含回答、评论、我的投票与可执行操作）")
    @GetMapping("/{questionId}")
    public QuestionDetailView detail(@PathVariable long questionId,
                                     @Parameter(hidden = true) Optional<Actor> actor) {
        return queries.detail(questionId, actor.orElse(null));
    }

    @Operation(summary = "编辑问题")
    @PutMapping("/{questionId}")
    public ResponseEntity<Void> edit(@Parameter(hidden = true) Actor actor, @PathVariable long questionId,
                                     @Valid @RequestBody QuestionRequest req) {
        commands.edit(actor, questionId, new EditQuestionCommand(req.title(), req.body(), req.tags()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "删除问题（软删除）")
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> delete(@Parameter(hidden = true) Actor actor, @PathVariable long questionId) {
        commands.delete(actor, questionId);
        return ResponseEntity.noContent().build();
    }

    public record QuestionRequest(Long courseId,
                                  @NotBlank @Size(max = 150) String title,
                                  @NotBlank @Size(max = 30000) String body,
                                  @NotNull @Size(min = 1, max = 5) List<String> tags) {
    }
}
