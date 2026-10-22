package com.campusoverflow.reputation.interfaces;

import com.campusoverflow.reputation.application.LeaderboardEntryView;
import com.campusoverflow.reputation.application.LeaderboardPeriod;
import com.campusoverflow.reputation.application.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "排行榜 Leaderboard")
@RestController
@RequestMapping("/api/v1/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboard;

    public LeaderboardController(LeaderboardService leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Operation(summary = "排行榜（period: week / month / term / all）")
    @GetMapping
    public List<LeaderboardEntryView> top(@RequestParam(defaultValue = "week") String period,
                                          @RequestParam(defaultValue = "20") int limit) {
        return leaderboard.top(LeaderboardPeriod.valueOf(period.toUpperCase(Locale.ROOT)), limit);
    }
}
