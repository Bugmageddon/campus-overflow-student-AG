package com.campusoverflow.qa.application.command;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.qa.api.event.ContentDeletedEvent;
import com.campusoverflow.qa.api.event.QuestionEditedEvent;
import com.campusoverflow.qa.api.event.QuestionPostedEvent;
import com.campusoverflow.qa.application.port.ContentRenderer;
import com.campusoverflow.qa.application.port.QuestionListCache;
import com.campusoverflow.qa.domain.MarkdownBody;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.QuestionRepository;
import com.campusoverflow.qa.domain.TagRepository;
import com.campusoverflow.qa.domain.Tags;
import com.campusoverflow.qa.domain.Title;
import com.campusoverflow.shared.audit.AuditTrail;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.event.EventIds;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.security.Actor;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用例：发布 / 编辑 / 删除问题。 */
@Service
public class QuestionCommandService {

    static final int PLAIN_TEXT_FOR_INDEX = 2000;

    private final QuestionRepository questions;
    private final TagRepository tags;
    private final IdentityApi identity;
    private final IntegrationEventPublisher events;
    private final ContentRenderer renderer;
    private final QuestionListCache listCache;
    private final AuditTrail audit;
    private final Clock clock;

    public QuestionCommandService(QuestionRepository questions, TagRepository tags, IdentityApi identity,
                                  IntegrationEventPublisher events, ContentRenderer renderer,
                                  QuestionListCache listCache, AuditTrail audit, Clock clock) {
        this.questions = questions;
        this.tags = tags;
        this.identity = identity;
        this.events = events;
        this.renderer = renderer;
        this.listCache = listCache;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public long post(Actor actor, PostQuestionCommand cmd) {
        if (cmd.courseId() != null && identity.findCourse(cmd.courseId()).isEmpty()) {
            throw new NotFoundException("ID-4002", "课程不存在");
        }
        Question question = Question.post(actor.userId(), cmd.courseId(), new Title(cmd.title()),
                MarkdownBody.ofQuestion(cmd.body()), Tags.of(cmd.tags()), clock.instant());
        Question saved = questions.save(question);
        tags.adjustUsage(saved.tags().names(), 1);
        events.publish(new QuestionPostedEvent(EventIds.next(), clock.instant(), saved.id(), saved.authorId(),
                saved.courseId(), saved.title().value(),
                renderer.toPlainText(saved.body().value(), PLAIN_TEXT_FOR_INDEX), saved.tags().names()));
        listCache.invalidateAll();
        return saved.id();
    }

    @Transactional
    public void edit(Actor actor, long questionId, EditQuestionCommand cmd) {
        Question question = load(questionId);
        boolean manages = identity.canManageCourse(actor.userId(), question.courseId());
        Set<String> before = new HashSet<>(question.tags().names());
        question.edit(actor, manages, new Title(cmd.title()), MarkdownBody.ofQuestion(cmd.body()),
                Tags.of(cmd.tags()), clock.instant());
        Question saved = questions.save(question);

        Set<String> after = new HashSet<>(saved.tags().names());
        Set<String> removed = new HashSet<>(before);
        removed.removeAll(after);
        Set<String> added = new HashSet<>(after);
        added.removeAll(before);
        if (!removed.isEmpty()) {
            tags.adjustUsage(removed, -1);
        }
        if (!added.isEmpty()) {
            tags.adjustUsage(added, 1);
        }

        events.publish(new QuestionEditedEvent(EventIds.next(), clock.instant(), saved.id(), saved.title().value(),
                renderer.toPlainText(saved.body().value(), PLAIN_TEXT_FOR_INDEX), saved.tags().names()));
        listCache.invalidateAll();
    }

    @Transactional
    public void delete(Actor actor, long questionId) {
        Question question = load(questionId);
        boolean manages = identity.canManageCourse(actor.userId(), question.courseId());
        question.softDelete(actor, manages, clock.instant());
        questions.save(question);
        tags.adjustUsage(question.tags().names(), -1);
        events.publish(new ContentDeletedEvent(EventIds.next(), clock.instant(), "QUESTION", questionId, questionId));
        audit.record("QUESTION_DELETED", actor.userId(), "QUESTION", String.valueOf(questionId), null);
        listCache.invalidateAll();
    }

    private Question load(long questionId) {
        return questions.findById(questionId)
                .filter(question -> !question.isDeleted())
                .orElseThrow(() -> new NotFoundException("QA-4001", "问题不存在"));
    }
}
