package com.campusoverflow.qa.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.qa.api.event.ContentDeletedEvent;
import com.campusoverflow.qa.api.event.QuestionEditedEvent;
import com.campusoverflow.qa.api.event.QuestionPostedEvent;
import com.campusoverflow.qa.application.fake.AppTestFixtures.FakeRenderer;
import com.campusoverflow.qa.application.fake.AppTestFixtures.RecordingTags;
import com.campusoverflow.qa.application.fake.QaTestFixtures.CountingCache;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryQuestions;
import com.campusoverflow.qa.application.fake.QaTestFixtures.RecordingAudit;
import com.campusoverflow.qa.application.fake.QaTestFixtures.RecordingEvents;
import com.campusoverflow.qa.application.fake.QaTestFixtures.StubIdentity;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestionCommandServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-26T08:00:00Z");

    private final InMemoryQuestions questions = new InMemoryQuestions();
    private final RecordingTags tags = new RecordingTags();
    private final StubIdentity identity = new StubIdentity()
            .withUser(1, "小明", Role.STUDENT)
            .withUser(2, "小红", Role.STUDENT);
    private final RecordingEvents events = new RecordingEvents();
    private final CountingCache cache = new CountingCache();
    private final RecordingAudit audit = new RecordingAudit();

    private final Actor author = new Actor(1, "小明", Role.STUDENT);
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private QuestionCommandService service;

    @BeforeEach
    void setUp() {
        service = new QuestionCommandService(questions, tags, identity, events,
                new FakeRenderer(), cache, audit, clock);
    }

    @Test
    void postSavesQuestionPublishesEventAndAdjustsTags() {
        long id = service.post(author, new PostQuestionCommand(100L,
                "这是一个超过十个字符的问题标题",
                "这是问题的正文内容，长度肯定超过二十个字符。",
                List.of("Java", "Spring")));

        assertThat(questions.findById(id)).isPresent();
        assertThat(tags.calls).hasSize(1);
        assertThat(tags.calls.get(0)).containsExactly("java", "spring");
        assertThat(tags.deltas).containsExactly(1);

        QuestionPostedEvent event = events.ofType(QuestionPostedEvent.class).get(0);
        assertThat(event.questionId()).isEqualTo(id);
        assertThat(event.authorId()).isEqualTo(author.userId());
        assertThat(event.courseId()).isEqualTo(100L);
        assertThat(event.tags()).containsExactly("java", "spring");
        assertThat(cache.invalidations).isEqualTo(1);
    }

    @Test
    void editAdjustsOnlyChangedTagsAndPublishesEvent() {
        long id = service.post(author, new PostQuestionCommand(100L,
                "这是一个超过十个字符的问题标题",
                "这是问题的正文内容，长度肯定超过二十个字符。",
                List.of("java", "spring")));

        service.edit(author, id, new EditQuestionCommand(
                "编辑之后仍然超过十个字符的标题",
                "编辑之后的正文内容，长度肯定还是超过二十个字符。",
                List.of("java", "redis")));

        assertThat(tags.calls).hasSize(3);
        assertThat(tags.calls.get(1)).containsExactly("spring");
        assertThat(tags.deltas.get(1)).isEqualTo(-1);
        assertThat(tags.calls.get(2)).containsExactly("redis");
        assertThat(tags.deltas.get(2)).isEqualTo(1);
        assertThat(events.ofType(QuestionEditedEvent.class)).hasSize(1);
        assertThat(cache.invalidations).isEqualTo(2);
    }

    @Test
    void deleteMarksDeletedPublishesEventAndAudits() {
        long id = service.post(author, new PostQuestionCommand(100L,
                "这是一个超过十个字符的问题标题",
                "这是问题的正文内容，长度肯定超过二十个字符。",
                List.of("java")));

        service.delete(author, id);

        assertThat(questions.findById(id).orElseThrow().isDeleted()).isTrue();
        ContentDeletedEvent event = events.ofType(ContentDeletedEvent.class).get(0);
        assertThat(event.targetType()).isEqualTo("QUESTION");
        assertThat(event.targetId()).isEqualTo(id);
        assertThat(audit.actions).contains("QUESTION_DELETED");
        assertThat(tags.deltas.get(tags.deltas.size() - 1)).isEqualTo(-1);
        assertThat(cache.invalidations).isEqualTo(2);
    }

    @Test
    void missingQuestionThrowsNotFound() {
        assertThatThrownBy(() -> service.delete(author, 999L))
                .isInstanceOf(NotFoundException.class);
    }
}
