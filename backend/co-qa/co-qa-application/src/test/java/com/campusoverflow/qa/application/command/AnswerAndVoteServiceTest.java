package com.campusoverflow.qa.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.AnswerSubmittedEvent;
import com.campusoverflow.qa.api.event.VoteCastEvent;
import com.campusoverflow.qa.application.fake.QaTestFixtures.CountingCache;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryAnswers;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryQuestions;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryVotes;
import com.campusoverflow.qa.application.fake.QaTestFixtures.RecordingAudit;
import com.campusoverflow.qa.application.fake.QaTestFixtures.RecordingEvents;
import com.campusoverflow.qa.application.fake.QaTestFixtures.StubIdentity;
import com.campusoverflow.qa.domain.MarkdownBody;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.qa.domain.Tags;
import com.campusoverflow.qa.domain.Title;
import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 应用服务用例测试：验证编排逻辑（权限判定入口、事件发布、缓存失效、审计）而非领域规则本身。 */
class AnswerAndVoteServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-08T06:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private final InMemoryQuestions questions = new InMemoryQuestions();
    private final InMemoryAnswers answers = new InMemoryAnswers();
    private final InMemoryVotes votes = new InMemoryVotes();
    private final RecordingEvents events = new RecordingEvents();
    private final CountingCache cache = new CountingCache();
    private final RecordingAudit audit = new RecordingAudit();
    private final StubIdentity identity = new StubIdentity()
            .withUser(1, "小明", Role.STUDENT).withUser(2, "小红", Role.STUDENT).withUser(9, "王老师", Role.TEACHER);

    private final Actor ming = new Actor(1, "小明", Role.STUDENT);
    private final Actor hong = new Actor(2, "小红", Role.STUDENT);

    private AnswerCommandService answerService;
    private VoteService voteService;
    private long questionId;

    @BeforeEach
    void setUp() {
        answerService = new AnswerCommandService(questions, answers, identity, events, cache, audit, clock);
        voteService = new VoteService(votes, questions, answers, events, clock);
        Question q = questions.save(Question.post(ming.userId(), 100L, new Title("红黑树插入后为什么必须旋转？"),
                MarkdownBody.ofQuestion("变色为什么不够？希望有直观解释，最好能给出一个反例。"),
                Tags.of(List.of("数据结构", "红黑树")), NOW));
        questionId = q.id();
    }

    @Test
    @DisplayName("提交回答：计数器用原子增量维护，并发布集成事件")
    void submitAnswerPublishesEventAndBumpsCounter() {
        long answerId = answerService.submit(hong, questionId, "关键在于黑高这个不变量，仅靠变色无法调整。");

        assertThat(questions.answerCountDeltas).containsEntry(questionId, 1);
        List<AnswerSubmittedEvent> submitted = events.ofType(AnswerSubmittedEvent.class);
        assertThat(submitted).hasSize(1);
        assertThat(submitted.get(0).answerId()).isEqualTo(answerId);
        assertThat(submitted.get(0).questionAuthorId()).isEqualTo(ming.userId());
        assertThat(cache.invalidations).isEqualTo(1);
    }

    @Test
    @DisplayName("采纳：只有提问者或课程管理者可以采纳，且会发布 AnswerAccepted 供声誉上下文结算")
    void onlyAskerOrCourseManagerCanAccept() {
        long answerId = answerService.submit(hong, questionId, "关键在于黑高这个不变量，仅靠变色无法调整。");

        assertThatThrownBy(() -> answerService.accept(hong, questionId, answerId))
                .isInstanceOf(ForbiddenException.class);

        answerService.accept(ming, questionId, answerId);

        List<AnswerAcceptedEvent> accepted = events.ofType(AnswerAcceptedEvent.class);
        assertThat(accepted).hasSize(1);
        assertThat(accepted.get(0).answerAuthorId()).isEqualTo(hong.userId());
        assertThat(accepted.get(0).acceptedById()).isEqualTo(ming.userId());
        assertThat(audit.actions).contains("ANSWER_ACCEPTED");
    }

    @Test
    @DisplayName("投票：改投时按“新值 − 旧值”计算增量，事件携带新旧值供声誉幂等结算")
    void changingVoteProducesCorrectDelta() {
        long answerId = answerService.submit(hong, questionId, "关键在于黑高这个不变量，仅靠变色无法调整。");

        assertThat(voteService.cast(ming, TargetType.ANSWER, answerId, 1).scoreDelta()).isEqualTo(1);
        assertThat(voteService.cast(ming, TargetType.ANSWER, answerId, 1).scoreDelta()).isZero(); // 幂等
        assertThat(voteService.cast(ming, TargetType.ANSWER, answerId, -1).scoreDelta()).isEqualTo(-2);
        assertThat(answers.scoreDeltas).containsEntry(answerId, -1);

        List<VoteCastEvent> voteEvents = events.ofType(VoteCastEvent.class);
        assertThat(voteEvents).hasSize(2);
        assertThat(voteEvents.get(1).oldValue()).isEqualTo(1);
        assertThat(voteEvents.get(1).newValue()).isEqualTo(-1);
    }

    @Test
    @DisplayName("不能给自己的内容投票")
    void cannotVoteOnOwnContent() {
        assertThatThrownBy(() -> voteService.cast(ming, TargetType.QUESTION, questionId, 1))
                .isInstanceOf(BusinessRuleException.class);
    }
}
