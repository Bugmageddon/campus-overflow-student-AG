package com.campusoverflow.reputation.interfaces;

import com.campusoverflow.reputation.application.BountyService;
import com.campusoverflow.reputation.application.BountyView;
import com.campusoverflow.shared.security.Actor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "悬赏 Bounty")
@RestController
@RequestMapping("/api/v1/questions/{questionId}/bounty")
public class BountyController {

    private final BountyService bounties;

    public BountyController(BountyService bounties) {
        this.bounties = bounties;
    }

    @Operation(summary = "问题的最近一次悬赏（无悬赏返回 204）")
    @GetMapping
    public ResponseEntity<BountyView> latest(@PathVariable long questionId) {
        return bounties.latest(questionId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "发起悬赏（课程教师/助教/管理员，冻结积分）")
    @PostMapping
    public ResponseEntity<Map<String, Long>> open(@Parameter(hidden = true) Actor actor, @PathVariable long questionId,
                                                  @Valid @RequestBody BountyRequest req) {
        long id = bounties.open(actor, questionId, req.points(), req.days());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    public record BountyRequest(@NotNull @Min(10) @Max(500) Integer points, @NotNull @Min(1) @Max(14) Integer days) {
    }
}
