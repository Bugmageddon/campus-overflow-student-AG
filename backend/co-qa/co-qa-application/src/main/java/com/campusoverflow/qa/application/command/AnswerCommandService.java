package com.campusoverflow.qa.application.command;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.AnswerEndorsedEvent;
import com.campusoverflow.qa.api.event.AnswerSubmittedEvent;
import com.campusoverflow.qa.api.event.ContentDeletedEvent;
import com.campusoverflow.qa.application.port.QuestionListCache;
import com.campusoverflow.qa.domain.Answer;
import com.campusoverflow.qa.domain.AnswerRepository;
import com.campusoverflow.qa.domain.MarkdownBody;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.QuestionRepository;
import com.campusoverflow.shared.audit.AuditTrail;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.event.EventIds;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.security.Actor;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用例：提交 / 编辑 / 删除回答，采纳最佳答案，教师认证。 */
@Service
public class AnswerCommandService {

    private final QuestionRepository questions;
    private final AnswerRepository answers;
    private final IdentityApi identity;
    private final IntegrationEventPublisher events;
    private final QuestionListCache listCache;
    private final AuditTrail audit;
    private final Clock clock;

    public AnswerCommandService(QuestionRepository questions, AnswerRepository answers, IdentityApi identity,
                                IntegrationEventPublisher events, QuestionListCache listCache, AuditTrail audit,
                                Clock clock) {
        this.questions = questions;
        this.answers = answers;
        this.identity = identity;
        this.events = events;
        this.listCache = listCache;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public long submit(Actor actor, long questionId, String body) {
        Question question = loadQuestion(questionId);
        Answer answer = Answer.submit(question, actor.userId(), MarkdownBody.ofAnswer(body), clock.instant());
        Answer saved = answers.save(answer);
        questions.adjustAnswerCount(questionId, 1);
        events.publish(new AnswerSubmittedEvent(EventIds.next(), clock.instant(), saved.id(), questionId,
                saved.authorId(), question.authorId(), question.title().value()));
        listCache.invalidateAll();
        return saved.id();
    }

    @Transactional
    public void edit(Actor actor, long answerId, String body) {
        Answer answer = loadAnswer(answerId);
        Question question = loadQuestion(answer.questionId());
        boolean manages = identity.canManageCourse(actor.userId(), question.courseId());
        answer.edit(actor, manages, MarkdownBody.ofAnswer(body), clock.instant());
        answers.save(answer);
    }

    @Transactional
    public void delete(Actor actor, long answerId) {
        Answer answer = loadAnswer(answerId);
        Question question = loadQuestion(answer.questionId());
        boolean manages = identity.canManageCourse(actor.userId(), question.courseId());
        answer.softDelete(actor, manages, question.isAccepted(answerId), clock.instant());
        answers.save(answer);
        questions.adjustAnswerCount(question.id(), -1);
        events.publish(new ContentDeletedEvent(EventIds.next(), clock.instant(), "ANSWER", answerId, question.id()));
        audit.record("ANSWER_DELETED", actor.userId(), "ANSWER", String.valueOf(answerId), null);
        listCache.invalidateAll();
    }

    /** 场景 R-2：采纳最佳答案。状态变更与 Outbox 事件在同一事务提交；声誉结算异步进行。 */
    @Transactional
    public void accept(Actor actor, long questionId, long answerId) {
        Question question = loadQuestion(questionId);
        Answer answer = loadAnswer(answerId);
        boolean manages = identity.canManageCourse(actor.userId(), question.courseId());
        question.accept(answer, actor, manages);
        questions.save(question);
        events.publish(new AnswerAcceptedEvent(EventIds.next(), clock.instant(), answer.id(), question.id(),
                answer.authorId(), question.authorId(), actor.userId(), question.title().value()));
        audit.record("ANSWER_ACCEPTED", actor.userId(), "ANSWER", String.valueOf(answerId), null);
        listCache.invalidateAll();
    }

    @Transactional
    public void endorse(Actor actor, long answerId) {
        Answer answer = loadAnswer(answerId);
        Question question = loadQuestion(answer.questionId());
        boolean manages = identity.canManageCourse(actor.userId(), question.courseId());
        answer.endorse(actor, manages);
        answers.save(answer);
        events.publish(new AnswerEndorsedEvent(EventIds.next(), clock.instant(), answer.id(), question.id(),
                answer.authorId(), actor.userId(), question.title().value()));
        audit.record("ANSWER_ENDORSED", actor.userId(), "ANSWER", String.valueOf(answerId), null);
    }

    private Question loadQuestion(long id) {
        return questions.findById(id)
                .filter(question -> !question.isDeleted())
                .orElseThrow(() -> new NotFoundException("QA-4001", "问题不存在"));
    }

    private Answer loadAnswer(long id) {
        return answers.findById(id)
                .filter(answer -> !answer.isDeleted())
                .orElseThrow(() -> new NotFoundException("QA-4002", "回答不存在"));
    }
}
