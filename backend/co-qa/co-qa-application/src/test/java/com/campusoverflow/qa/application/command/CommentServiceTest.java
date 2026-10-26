package com.campusoverflow.qa.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.qa.api.event.CommentPostedEvent;
import com.campusoverflow.qa.application.fake.AppTestFixtures.InMemoryComments;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryAnswers;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryQuestions;
import com.campusoverflow.qa.application.fake.QaTestFixtures.RecordingEvents;
import com.campusoverflow.qa.domain.Answer;
import com.campusoverflow.qa.domain.Comment;
import com.campusoverflow.qa.domain.MarkdownBody;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.Tags;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.qa.domain.Title;
import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommentServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-26T08:00:00Z");

    private final InMemoryComments comments = new InMemoryComments();
    private final InMemoryQuestions questions = new InMemoryQuestions();
    private final InMemoryAnswers answers = new InMemoryAnswers();
    private final RecordingEvents events = new RecordingEvents();
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private final Actor reader = new Actor(2, "小红", Role.STUDENT);

    private CommentService service;
    private Question question;
    private Answer answer;

    @BeforeEach
    void setUp() {
        service = new CommentService(comments, questions, answers, events, clock);
        question = questions.save(Question.post(1, 100L,
                new Title("这是一个超过十个字符的问题标题"),
                MarkdownBody.ofQuestion("这是问题的正文内容，长度肯定超过二十个字符。"),
                Tags.of(List.of("java")), NOW));
        answer = answers.save(Answer.submit(question, 2,
                MarkdownBody.ofAnswer("这是足够长的回答内容"), NOW));
    }

    @Test
    void commentOnQuestionUsesQuestionIdAndPublishesEvent() {
        long id = service.post(reader, new PostCommentCommand(TargetType.QUESTION, question.id(), null, "评论内容"));

        Comment saved = comments.findById(id).orElseThrow();
        assertThat(saved.questionId()).isEqualTo(question.id());
        assertThat(saved.targetType()).isEqualTo(TargetType.QUESTION);
        assertThat(saved.parentId()).isNull();
        assertThat(events.ofType(CommentPostedEvent.class).get(0).body()).isEqualTo("评论内容");
    }

    @Test
    void commentOnAnswerResolvesQuestionIdFromAnswer() {
        long id = service.post(reader, new PostCommentCommand(TargetType.ANSWER, answer.id(), null, "回答的评论"));

        Comment saved = comments.findById(id).orElseThrow();
        assertThat(saved.questionId()).isEqualTo(question.id());
        assertThat(saved.targetType()).isEqualTo(TargetType.ANSWER);
    }

    @Test
    void replyIsLimitedToOneLevel() {
        long topId = service.post(reader, new PostCommentCommand(TargetType.QUESTION, question.id(), null, "顶层评论"));
        long replyId = service.post(reader, new PostCommentCommand(TargetType.QUESTION, question.id(), topId, "一层回复"));

        assertThat(comments.findById(replyId).orElseThrow().parentId()).isEqualTo(topId);
        assertThatThrownBy(() -> service.post(reader,
                new PostCommentCommand(TargetType.QUESTION, question.id(), replyId, "二层回复")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void missingTargetThrowsNotFound() {
        assertThatThrownBy(() -> service.post(reader,
                new PostCommentCommand(TargetType.QUESTION, 999L, null, "评论内容")))
                .isInstanceOf(NotFoundException.class);
    }
}
