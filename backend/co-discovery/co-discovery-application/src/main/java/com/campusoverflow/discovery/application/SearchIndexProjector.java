package com.campusoverflow.discovery.application;

import com.campusoverflow.discovery.domain.search.SearchDocument;
import com.campusoverflow.discovery.domain.search.SearchIndex;
import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.AnswerSubmittedEvent;
import com.campusoverflow.qa.api.event.ContentDeletedEvent;
import com.campusoverflow.qa.api.event.QuestionEditedEvent;
import com.campusoverflow.qa.api.event.QuestionPostedEvent;
import com.campusoverflow.qa.api.event.VoteCastEvent;
import com.campusoverflow.reputation.api.event.BountyAwardedEvent;
import com.campusoverflow.reputation.api.event.BountyClosedEvent;
import com.campusoverflow.reputation.api.event.BountyOpenedEvent;
import com.campusoverflow.shared.event.IntegrationEvent;
import com.campusoverflow.shared.event.ProcessedEventStore;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 事件投影器：把上游事件折叠为搜索投影（dis_search_doc）。
 * 计数器类更新不是天然幂等的，因此每个事件先经 ProcessedEventStore 去重（与投影更新同一事务）。
 */
@Component
public class SearchIndexProjector {

    static final String CONSUMER = "discovery.search-index";

    private final SearchIndex index;
    private final ProcessedEventStore processed;

    public SearchIndexProjector(SearchIndex index, ProcessedEventStore processed) {
        this.index = index;
        this.processed = processed;
    }

    private boolean firstTime(IntegrationEvent e) {
        return processed.markProcessed(e.eventId(), CONSUMER);
    }

    @EventListener
    @Transactional
    public void on(QuestionPostedEvent e) {
        if (!firstTime(e)) {
            return;
        }
        index.insert(new SearchDocument(e.questionId(), e.courseId(), e.authorId(), e.title(), e.plainText(),
                e.tags(), e.occurredAt()));
    }

    @EventListener
    @Transactional
    public void on(QuestionEditedEvent e) {
        if (!firstTime(e)) {
            return;
        }
        index.updateText(e.questionId(), e.title(), e.plainText(), e.tags(), e.occurredAt());
    }

    @EventListener
    @Transactional
    public void on(AnswerSubmittedEvent e) {
        if (!firstTime(e)) {
            return;
        }
        index.adjustAnswerCount(e.questionId(), 1, e.occurredAt());
    }

    @EventListener
    @Transactional
    public void on(ContentDeletedEvent e) {
        if (!firstTime(e)) {
            return;
        }
        if ("QUESTION".equals(e.targetType())) {
            index.markDeleted(e.questionId());
        } else if ("ANSWER".equals(e.targetType())) {
            index.adjustAnswerCount(e.questionId(), -1, e.occurredAt());
        }
    }

    @EventListener
    @Transactional
    public void on(AnswerAcceptedEvent e) {
        if (!firstTime(e)) {
            return;
        }
        index.markAccepted(e.questionId(), e.occurredAt());
    }

    @EventListener
    @Transactional
    public void on(VoteCastEvent e) {
        if (!firstTime(e)) {
            return;
        }
        if ("QUESTION".equals(e.targetType())) {
            index.adjustScore(e.questionId(), e.newValue() - e.oldValue());
        }
    }

    @EventListener
    @Transactional
    public void on(BountyOpenedEvent e) {
        if (!firstTime(e)) {
            return;
        }
        index.setBountyOpen(e.questionId(), true);
    }

    @EventListener
    @Transactional
    public void on(BountyAwardedEvent e) {
        if (!firstTime(e)) {
            return;
        }
        index.setBountyOpen(e.questionId(), false);
    }

    @EventListener
    @Transactional
    public void on(BountyClosedEvent e) {
        if (!firstTime(e)) {
            return;
        }
        index.setBountyOpen(e.questionId(), false);
    }
}
