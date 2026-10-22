package com.campusoverflow.qa.infrastructure.query;

import com.campusoverflow.qa.application.port.QaReadModel;
import com.campusoverflow.qa.application.query.AnswerRow;
import com.campusoverflow.qa.application.query.CommentRow;
import com.campusoverflow.qa.application.query.QuestionRow;
import com.campusoverflow.qa.application.query.TagView;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.util.UtcTime;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/** 读模型的 JDBC 实现：只读 qa_* 表，不跨上下文 JOIN。 */
@Component
public class JdbcQaReadModel implements QaReadModel {

    private static final String QUESTION_COLUMNS = """
            id, author_id, course_id, title, %s AS body, accepted_answer_id, score, answer_count, view_count,
            created_at, updated_at
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcQaReadModel(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public PageResult<QuestionRow> listQuestions(Long courseId, PageRequest page) {
        String where = " WHERE deleted_at IS NULL" + (courseId != null ? " AND course_id = :courseId" : "");
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("courseId", courseId).addValue("limit", page.size()).addValue("offset", page.offset());
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM qa_question" + where, p, Long.class);
        List<QuestionRow> rows = jdbc.query("SELECT " + QUESTION_COLUMNS.formatted("LEFT(body, 600)")
                + " FROM qa_question" + where + " ORDER BY created_at DESC, id DESC LIMIT :limit OFFSET :offset", p,
                (rs, i) -> mapQuestion(rs, List.of()));
        return new PageResult<>(withTags(rows), total == null ? 0 : total, page.page(), page.size());
    }

    @Override
    public Optional<QuestionRow> findQuestion(long questionId) {
        List<QuestionRow> rows = jdbc.query("SELECT " + QUESTION_COLUMNS.formatted("body")
                        + " FROM qa_question WHERE id = :id AND deleted_at IS NULL",
                new MapSqlParameterSource("id", questionId), (rs, i) -> mapQuestion(rs, List.of()));
        return withTags(rows).stream().findFirst();
    }

    @Override
    public List<AnswerRow> findAnswers(long questionId) {
        return jdbc.query("""
                        SELECT id, author_id, body, score, endorsed_by, created_at, updated_at
                        FROM qa_answer WHERE question_id = :qid AND deleted_at IS NULL
                        """, new MapSqlParameterSource("qid", questionId),
                (rs, i) -> new AnswerRow(rs.getLong("id"), rs.getLong("author_id"), rs.getString("body"),
                        rs.getInt("score"), nullableLong(rs, "endorsed_by"), instant(rs, "created_at"),
                        instant(rs, "updated_at")));
    }

    @Override
    public List<CommentRow> findComments(long questionId) {
        return jdbc.query("""
                        SELECT id, target_type, target_id, author_id, parent_id, body, created_at
                        FROM qa_comment WHERE question_id = :qid ORDER BY created_at, id
                        """, new MapSqlParameterSource("qid", questionId),
                (rs, i) -> new CommentRow(rs.getLong("id"), rs.getString("target_type"), rs.getLong("target_id"),
                        rs.getLong("author_id"), nullableLong(rs, "parent_id"), rs.getString("body"),
                        instant(rs, "created_at")));
    }

    @Override
    public Map<String, Integer> findMyVotes(long voterId, long questionId, Collection<Long> answerIds) {
        Map<String, Integer> result = new HashMap<>();
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("voter", voterId).addValue("qid", questionId)
                .addValue("aids", answerIds);
        String sql = "SELECT target_type, target_id, value FROM qa_vote WHERE voter_id = :voter AND "
                + (answerIds.isEmpty()
                ? "target_type = 'QUESTION' AND target_id = :qid"
                : "((target_type = 'QUESTION' AND target_id = :qid) OR (target_type = 'ANSWER' AND target_id IN (:aids)))");
        jdbc.query(sql, p, rs -> {
            result.put(rs.getString("target_type") + ":" + rs.getLong("target_id"), rs.getInt("value"));
        });
        return result;
    }

    @Override
    public List<TagView> popularTags(int limit) {
        return jdbc.query("""
                        SELECT name, question_count FROM qa_tag WHERE question_count > 0
                        ORDER BY question_count DESC, name LIMIT :limit
                        """, new MapSqlParameterSource("limit", limit),
                (rs, i) -> new TagView(rs.getString("name"), rs.getLong("question_count")));
    }

    private List<QuestionRow> withTags(List<QuestionRow> rows) {
        if (rows.isEmpty()) {
            return rows;
        }
        Map<Long, List<String>> tags = new HashMap<>();
        jdbc.query("SELECT question_id, tag FROM qa_question_tag WHERE question_id IN (:ids) ORDER BY tag",
                new MapSqlParameterSource("ids", rows.stream().map(QuestionRow::id).toList()),
                rs -> {
                    tags.computeIfAbsent(rs.getLong("question_id"), k -> new ArrayList<>()).add(rs.getString("tag"));
                });
        return rows.stream().map(r -> new QuestionRow(r.id(), r.authorId(), r.courseId(), r.title(), r.body(),
                tags.getOrDefault(r.id(), List.of()), r.acceptedAnswerId(), r.score(), r.answerCount(),
                r.viewCount(), r.createdAt(), r.updatedAt())).toList();
    }

    private static QuestionRow mapQuestion(ResultSet rs, List<String> tags) throws SQLException {
        return new QuestionRow(rs.getLong("id"), rs.getLong("author_id"), nullableLong(rs, "course_id"),
                rs.getString("title"), rs.getString("body"), tags, nullableLong(rs, "accepted_answer_id"),
                rs.getInt("score"), rs.getInt("answer_count"), rs.getInt("view_count"), instant(rs, "created_at"),
                instant(rs, "updated_at"));
    }

    private static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long v = rs.getLong(column);
        return rs.wasNull() ? null : v;
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        return UtcTime.fromDb(rs.getObject(column, LocalDateTime.class));
    }
}
