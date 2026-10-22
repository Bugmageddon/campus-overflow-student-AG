package com.campusoverflow.reputation.interfaces;

import com.campusoverflow.reputation.application.LedgerEntryView;
import com.campusoverflow.reputation.application.ReputationProfileView;
import com.campusoverflow.reputation.application.ReputationQueryService;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "声誉 Reputation")
@RestController
@RequestMapping("/api/v1/users/{userId}/reputation")
public class ReputationController {

    private final ReputationQueryService queries;

    public ReputationController(ReputationQueryService queries) {
        this.queries = queries;
    }

    @Operation(summary = "声誉档案（声誉值、徽章；本人可见积分）")
    @GetMapping
    public ReputationProfileView profile(@PathVariable long userId, @Parameter(hidden = true) Optional<Actor> actor) {
        return queries.profile(userId, actor.orElse(null));
    }

    @Operation(summary = "声誉流水（仅本人或管理员）")
    @GetMapping("/ledger")
    public PageResult<LedgerEntryView> ledger(@PathVariable long userId, @Parameter(hidden = true) Actor actor,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return queries.ledger(userId, actor, new PageRequest(page, size));
    }
}
