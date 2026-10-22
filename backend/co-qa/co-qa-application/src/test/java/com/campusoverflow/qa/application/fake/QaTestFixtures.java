package com.campusoverflow.qa.application.fake;

import com.campusoverflow.identity.api.CourseSummary;
import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.qa.application.port.QuestionListCache;
import com.campusoverflow.qa.application.query.QuestionSummaryView;
import com.campusoverflow.qa.domain.Answer;
import com.campusoverflow.qa.domain.AnswerRepository;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.QuestionRepository;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.qa.domain.Vote;
import com.campusoverflow.qa.domain.VoteRepository;
import com.campusoverflow.shared.audit.AuditTrail;
import com.campusoverflow.shared.event.IntegrationEvent;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Role;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 手写测试替身（Test Double）。应用服务只依赖端口接口，因此不需要 Spring 容器、不需要数据库，
 * 单元测试可以在毫秒级完成——这正是“可测试性”作为驱动性架构特征带来的收益。
 */
public final class QaTestFixtures {

    private QaTestFixtures() {
    }

    public static class InMemoryQuestions implements QuestionRepository {
        private final Map<Long, Question> store = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();
        public final Map<Long, Integer> answerCountDeltas = new HashMap<>();
        public final Map<Long, Integer> scoreDeltas = new HashMap<>();

        @Override
        public Optional<Question> findById(long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Question save(Question question) {
            if (question.id() == null) {
                question.assignId(sequence.incrementAndGet());
            }
            store.put(question.id(), question);
            return question;
        }

        @Override
        public void adjustAnswerCount(long questionId, int delta) {
            answerCountDeltas.merge(questionId, delta, Integer::sum);
        }

        @Override
        public void adjustScore(long questionId, int delta) {
            scoreDeltas.merge(questionId, delta, Integer::sum);
        }
    }

    public static class InMemoryAnswers implements AnswerRepository {
        private final Map<Long, Answer> store = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();
        public final Map<Long, Integer> scoreDeltas = new HashMap<>();

        @Override
        public Optional<Answer> findById(long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Answer save(Answer answer) {
            if (answer.id() == null) {
                answer.assignId(sequence.incrementAndGet());
            }
            store.put(answer.id(), answer);
            return answer;
        }

        @Override
        public void adjustScore(long answerId, int delta) {
            scoreDeltas.merge(answerId, delta, Integer::sum);
        }
    }

    public static class InMemoryVotes implements VoteRepository {
        private final Map<String, Vote> store = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public Optional<Vote> find(long voterId, TargetType targetType, long targetId) {
            return Optional.ofNullable(store.get(key(voterId, targetType, targetId)));
        }

        @Override
        public Vote save(Vote vote) {
            if (vote.id() == null) {
                vote.assignId(sequence.incrementAndGet());
            }
            store.put(key(vote.voterId(), vote.targetType(), vote.targetId()), vote);
            return vote;
        }

        @Override
        public void delete(Vote vote) {
            store.remove(key(vote.voterId(), vote.targetType(), vote.targetId()));
        }

        private static String key(long voterId, TargetType type, long targetId) {
            return voterId + ":" + type + ":" + targetId;
        }
    }

    /** 记录所有已发布事件，便于断言“领域动作 → 集成事件”的契约。 */
    public static class RecordingEvents implements IntegrationEventPublisher {
        public final List<IntegrationEvent> published = new ArrayList<>();

        @Override
        public void publish(IntegrationEvent event) {
            published.add(event);
        }

        public <T extends IntegrationEvent> List<T> ofType(Class<T> type) {
            return published.stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
        }
    }

    public static class StubIdentity implements IdentityApi {
        private final Map<Long, UserSummary> users = new HashMap<>();
        private final Map<Long, Boolean> courseManagers = new HashMap<>();

        public StubIdentity withUser(long id, String name, Role role) {
            users.put(id, new UserSummary(id, name, role, true, "计算机工程学院"));
            return this;
        }

        public StubIdentity withCourseManager(long userId) {
            courseManagers.put(userId, true);
            return this;
        }

        @Override
        public Optional<UserSummary> findUser(long userId) {
            return Optional.ofNullable(users.get(userId));
        }

        @Override
        public Map<Long, UserSummary> findUsers(Collection<Long> userIds) {
            Map<Long, UserSummary> result = new HashMap<>();
            userIds.forEach(id -> findUser(id).ifPresent(u -> result.put(id, u)));
            return result;
        }

        @Override
        public Optional<UserSummary> findByDisplayName(String displayName) {
            return users.values().stream().filter(u -> u.displayName().equals(displayName)).findFirst();
        }

        @Override
        public Optional<CourseSummary> findCourse(long courseId) {
            return Optional.of(new CourseSummary(courseId, "CS2001", "数据结构", "2026-秋", 100L));
        }

        @Override
        public boolean canManageCourse(long userId, Long courseId) {
            return courseManagers.getOrDefault(userId, false);
        }
    }

    public static class CountingCache implements QuestionListCache {
        public int invalidations;

        @Override
        public Optional<PageResult<QuestionSummaryView>> get(Long courseId, PageRequest page) {
            return Optional.empty();
        }

        @Override
        public void put(Long courseId, PageRequest page, PageResult<QuestionSummaryView> value) {
        }

        @Override
        public void invalidateAll() {
            invalidations++;
        }
    }

    public static class RecordingAudit implements AuditTrail {
        public final List<String> actions = new ArrayList<>();

        @Override
        public void record(String action, Long actorId, String targetType, String targetId, String detail) {
            actions.add(action);
        }
    }
}
