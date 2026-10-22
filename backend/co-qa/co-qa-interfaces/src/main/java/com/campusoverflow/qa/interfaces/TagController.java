package com.campusoverflow.qa.interfaces;

import com.campusoverflow.qa.application.query.QuestionQueryService;
import com.campusoverflow.qa.application.query.TagView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "标签 Tag")
@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final QuestionQueryService queries;

    public TagController(QuestionQueryService queries) {
        this.queries = queries;
    }

    @Operation(summary = "热门标签")
    @GetMapping("/popular")
    public List<TagView> popular(@RequestParam(defaultValue = "20") int limit) {
        return queries.popularTags(limit);
    }
}
