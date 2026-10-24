package com.campusoverflow.qa.application.query;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.qa.application.port.ContentRenderer;
import com.campusoverflow.qa.application.port.QaReadModel;
import com.campusoverflow.qa.application.port.QuestionListCache;
import com.campusoverflow.qa.application.port.ViewCounter;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 问答查询用例（读路径）。 */
@Service
@Transactional(readOnly = true)
public class QuestionQueryService {

    private static final int EXCERPT_LENGTH = 160;

    private final QaReadModel readModel;
    private final IdentityApi identity;
    private final ContentRenderer renderer;
    private final ViewCounter viewCounter;
    private final QuestionListCache cache;

    public QuestionQueryService(QaReadModel readModel, IdentityApi identity, ContentRenderer renderer,
                                ViewCounter viewCounter, QuestionListCache cache) {
        this.readModel = readModel;
        this.identity = identity;
        this.renderer = renderer;
        this.viewCounter = viewCounter;
        this.cache = cache;
    }

    public PageResult<QuestionSummaryView> list(Long courseId, PageRequest page) {
        var cached = cache.get(courseId, page);
        if (cached.isPresent()) {
            return cached.get();
        }
        PageResult<QuestionRow> rows = readModel.listQuestions(courseId, page);
        Map<Long, UserSummary> users = identity.findUsers(
                rows.items().stream().map(QuestionRow::authorId).collect(Collectors.toSet()));
        PageResult<QuestionSummaryView> result = rows.map(row -> new QuestionSummaryView(
                row.id(), row.title(), renderer.toPlainText(row.body(), EXCERPT_LENGTH), row.tags(), row.courseId(),
                AuthorView.of(row.authorId(), users.get(row.authorId())), row.score(), row.answerCount(),
                row.viewCount(), row.acceptedAnswerId() != null, row.createdAt()));
        cache.put(courseId, page, result);
        return result;
    }

    public QuestionDetailView detail(long questionId, Actor actorOrNull) {
        QuestionRow row = readModel.findQuestion(questionId)
                .orElseThrow(() -> new NotFoundException("QA-4001", "问题不存在"));
        viewCounter.increment(questionId);

        List<AnswerRow> answerRows = readModel.findAnswers(questionId);
        List<CommentRow> commentRows = readModel.findComments(questionId);
        List<Long> answerIds = answerRows.stream().map(AnswerRow::id).toList();

        Set<Long> userIds = new HashSet<>();
        userIds.add(row.authorId());
        answerRows.forEach(answer -> userIds.add(answer.authorId()));
        commentRows.forEach(comment -> userIds.add(comment.authorId()));
        answerRows.stream().map(AnswerRow::endorsedBy).filter(Objects::nonNull).forEach(userIds::add);
        Map<Long, UserSummary> users = identity.findUsers(userIds);

        Map<String, Integer> votes = actorOrNull == null
                ? Map.of()
                : readModel.findMyVotes(actorOrNull.userId(), questionId, answerIds);
        boolean manages = actorOrNull != null && identity.canManageCourse(actorOrNull.userId(), row.courseId());
        boolean canEdit = actorOrNull != null && (actorOrNull.userId() == row.authorId() || manages);
        PermissionsView permissions = new PermissionsView(canEdit, canEdit,
                canEdit && row.acceptedAnswerId() == null, manages);

        List<CommentView> questionComments = commentRows.stream()
                .filter(comment -> "QUESTION".equals(comment.targetType()) && comment.targetId() == questionId)
                .map(comment -> toView(comment, users))
                .toList();

        Comparator<AnswerRow> order = Comparator
                .comparing((AnswerRow answer) -> !Long.valueOf(answer.id()).equals(row.acceptedAnswerId()))
                .thenComparing(Comparator.comparingInt(AnswerRow::score).reversed())
                .thenComparing(AnswerRow::createdAt);

        List<AnswerView> answers = answerRows.stream().sorted(order).map(answer -> new AnswerView(
                answer.id(), answer.body(), renderer.toSafeHtml(answer.body()),
                AuthorView.of(answer.authorId(), users.get(answer.authorId())), answer.score(),
                Long.valueOf(answer.id()).equals(row.acceptedAnswerId()),
                answer.endorsedBy() == null ? null : AuthorView.of(answer.endorsedBy(), users.get(answer.endorsedBy())),
                votes.getOrDefault("ANSWER:" + answer.id(), 0),
                actorOrNull != null && (actorOrNull.userId() == answer.authorId() || manages),
                answer.createdAt(), answer.updatedAt(),
                commentRows.stream()
                        .filter(comment -> "ANSWER".equals(comment.targetType()) && comment.targetId() == answer.id())
                        .map(comment -> toView(comment, users))
                        .toList())).toList();

        CourseRefView course = row.courseId() == null ? null : identity.findCourse(row.courseId())
                .map(item -> new CourseRefView(item.id(), item.code(), item.name()))
                .orElse(null);

        return new QuestionDetailView(row.id(), row.title(), row.body(), renderer.toSafeHtml(row.body()),
                row.tags(), course, AuthorView.of(row.authorId(), users.get(row.authorId())), row.score(),
                row.answerCount(), row.viewCount(), row.acceptedAnswerId(),
                votes.getOrDefault("QUESTION:" + row.id(), 0), row.createdAt(), row.updatedAt(), permissions,
                questionComments, answers);
    }

    public List<TagView> popularTags(int limit) {
        return readModel.popularTags(Math.max(1, Math.min(limit, 50)));
    }

    private CommentView toView(CommentRow comment, Map<Long, UserSummary> users) {
        return new CommentView(comment.id(), AuthorView.of(comment.authorId(), users.get(comment.authorId())),
                comment.parentId(), comment.body(), comment.createdAt());
    }
}
