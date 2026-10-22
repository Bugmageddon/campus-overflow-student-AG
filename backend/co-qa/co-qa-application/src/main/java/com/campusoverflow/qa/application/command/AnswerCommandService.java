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
        // TODO(S2)：实现 AnswerCommandService.submit——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：AnswerCommandService.submit 尚未实现");
    }

    @Transactional
    public void edit(Actor actor, long answerId, String body) {
        // TODO(S2)：实现 AnswerCommandService.edit——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：AnswerCommandService.edit 尚未实现");
    }

    @Transactional
    public void delete(Actor actor, long answerId) {
        // TODO(S2)：实现 AnswerCommandService.delete——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：AnswerCommandService.delete 尚未实现");
    }

    /** 场景 R-2：采纳最佳答案。状态变更与 Outbox 事件在同一事务提交；声誉结算异步进行。 */
    @Transactional
    public void accept(Actor actor, long questionId, long answerId) {
        // TODO(S2)：实现 AnswerCommandService.accept——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：AnswerCommandService.accept 尚未实现");
    }

    @Transactional
    public void endorse(Actor actor, long answerId) {
        // TODO(S2)：实现 AnswerCommandService.endorse——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：AnswerCommandService.endorse 尚未实现");
    }

    private Question loadQuestion(long id) {
        // TODO(S2)：实现 AnswerCommandService.loadQuestion——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：AnswerCommandService.loadQuestion 尚未实现");
    }

    private Answer loadAnswer(long id) {
        // TODO(S2)：实现 AnswerCommandService.loadAnswer——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：AnswerCommandService.loadAnswer 尚未实现");
    }
}
