package com.campusoverflow.qa.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.qa.application.fake.AppTestFixtures.CountingViewCounter;
import com.campusoverflow.qa.application.fake.AppTestFixtures.FakeQaReadModel;
import com.campusoverflow.qa.application.fake.AppTestFixtures.FakeRenderer;
import com.campusoverflow.qa.application.fake.AppTestFixtures.MemoryCache;
import com.campusoverflow.qa.application.fake.QaTestFixtures.StubIdentity;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestionQueryServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-26T08:00:00Z");

    private final FakeQaReadModel readModel = new FakeQaReadModel();
    private final StubIdentity identity = new StubIdentity()
            .withUser(1, "小明", Role.STUDENT)
            .withUser(2, "小红", Role.STUDENT);
    private final CountingViewCounter views = new CountingViewCounter();
    private final MemoryCache cache = new MemoryCache();

    private QuestionQueryService service;
    private QuestionRow questionRow;

    @BeforeEach
    void setUp() {
        service = new QuestionQueryService(readModel, identity, new FakeRenderer(), views, cache);
        questionRow = new QuestionRow(10L, 1L, 100L, "问题标题", "问题正文", List.of("java"),
                2L, 5, 3, 7, NOW, NOW);
    }

    @Test
    void listMapsRowsAndFillsCache() {
        readModel.listResult = new PageResult<>(List.of(questionRow), 1L, 1, 10);

        PageResult<QuestionSummaryView> page = service.list(100L, new PageRequest(1, 10));

        assertThat(page.items()).hasSize(1);
        QuestionSummaryView item = page.items().get(0);
        assertThat(item.author().displayName()).isEqualTo("小明");
        assertThat(item.excerpt()).isEqualTo("问题正文");
        assertThat(item.accepted()).isTrue();
        assertThat(cache.puts).isEqualTo(1);
    }

    @Test
    void listUsesCacheOnSecondCall() {
        readModel.listResult = new PageResult<>(List.of(questionRow), 1L, 1, 10);

        service.list(100L, new PageRequest(1, 10));
        service.list(100L, new PageRequest(1, 10));

        assertThat(readModel.listCalls).isEqualTo(1);
    }

    @Test
    void detailSortsAcceptedAnswerFirstAndBuildsPermissions() {
        readModel.question = Optional.of(questionRow);
        readModel.answers = List.of(
                new AnswerRow(1L, 2L, "回答一", 5, null, NOW, NOW),
                new AnswerRow(2L, 2L, "回答二", 10, 2L, NOW, NOW));
        readModel.comments = List.of(
                new CommentRow(1L, "QUESTION", 10L, 2L, null, "问题评论", NOW),
                new CommentRow(2L, "ANSWER", 2L, 1L, null, "回答评论", NOW));
        readModel.myVotes = Map.of("QUESTION:10", 1, "ANSWER:2", -1);

        QuestionDetailView view = service.detail(10L, new Actor(2, "小红", Role.STUDENT));

        assertThat(view.answers()).extracting(AnswerView::id).containsExactly(2L, 1L);
        assertThat(view.answers().get(0).accepted()).isTrue();
        assertThat(view.answers().get(0).comments()).hasSize(1);
        assertThat(view.comments()).hasSize(1);
        assertThat(view.myVote()).isEqualTo(1);
        assertThat(view.permissions().canEdit()).isFalse();
        assertThat(views.count).isEqualTo(1);
    }

    @Test
    void questionAuthorCanEditButCannotAcceptAgain() {
        readModel.question = Optional.of(questionRow);

        QuestionDetailView view = service.detail(10L, new Actor(1, "小明", Role.STUDENT));

        assertThat(view.permissions().canEdit()).isTrue();
        assertThat(view.permissions().canAccept()).isFalse();
    }

    @Test
    void missingQuestionThrowsNotFound() {
        readModel.question = Optional.empty();

        assertThatThrownBy(() -> service.detail(999L, null))
                .isInstanceOf(NotFoundException.class);
        assertThat(views.count).isZero();
    }

    @Test
    void popularTagsClampsLimitBetweenOneAndFifty() {
        readModel.tags = List.of(new TagView("java", 3));

        assertThat(service.popularTags(100)).hasSize(1);
        assertThat(readModel.lastTagLimit).isEqualTo(50);

        service.popularTags(0);
        assertThat(readModel.lastTagLimit).isEqualTo(1);
    }
}
