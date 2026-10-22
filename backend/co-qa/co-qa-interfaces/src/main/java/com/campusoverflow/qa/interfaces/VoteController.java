package com.campusoverflow.qa.interfaces;

import com.campusoverflow.qa.application.command.VoteService;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.shared.security.Actor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "投票 Vote")
@RestController
@RequestMapping("/api/v1/votes")
public class VoteController {

    private final VoteService votes;

    public VoteController(VoteService votes) {
        this.votes = votes;
    }

    @Operation(summary = "投票（value: 1 赞成，-1 反对，0 撤销）——幂等 PUT")
    @PutMapping
    public VoteService.VoteResult cast(@Parameter(hidden = true) Actor actor, @Valid @RequestBody VoteRequest req) {
        return votes.cast(actor, req.targetType(), req.targetId(), req.value());
    }

    public record VoteRequest(@NotNull TargetType targetType, @NotNull Long targetId,
                              @NotNull @Min(-1) @Max(1) Integer value) {
    }
}
