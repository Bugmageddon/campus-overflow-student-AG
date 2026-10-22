package com.campusoverflow.discovery.infrastructure.search;

import com.campusoverflow.discovery.domain.search.SearchDocument;
import com.campusoverflow.discovery.domain.search.SearchIndex;
import com.campusoverflow.shared.util.UtcTime;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** 搜索投影写端（dis_search_doc / dis_question_tag）。 */
@Repository
public class JdbcSearchIndex implements SearchIndex {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcSearchIndex(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insert(SearchDocument d) {
        jdbc.update("""
                INSERT INTO dis_search_doc (question_id, course_id, author_id, title, body_text, tag_count, score,
                                            answer_count, accepted, bounty_open, deleted, created_at, last_activity_at)
                VALUES (:id, :courseId, :authorId, :title, :body, :tagCount, 0, 0, FALSE, FALSE, FALSE, :at, :at)
                ON DUPLICATE KEY UPDATE title = :title, body_text = :body, tag_count = :tagCount
                """, new MapSqlParameterSource()
                .addValue("id", d.questionId()).addValue("courseId", d.courseId()).addValue("authorId", d.authorId())
                .addValue("title", d.title()).addValue("body", d.bodyText()).addValue("tagCount", d.tags().size())
                .addValue("at", UtcTime.toDb(d.createdAt())));
        replaceTags(d.questionId(), d.tags());
    }

    @Override
    public void updateText(long questionId, String title, String bodyText, List<String> tags, Instant at) {
        jdbc.update("""
                UPDATE dis_search_doc SET title = :title, body_text = :body, tag_count = :tagCount,
                       last_activity_at = :at WHERE question_id = :id
                """, new MapSqlParameterSource().addValue("id", questionId).addValue("title", title)
                .addValue("body", bodyText).addValue("tagCount", tags.size()).addValue("at", UtcTime.toDb(at)));
        replaceTags(questionId, tags);
    }

    @Override
    public void adjustAnswerCount(long questionId, int delta, Instant at) {
        jdbc.update("""
                UPDATE dis_search_doc SET answer_count = GREATEST(answer_count + :delta, 0),
                       last_activity_at = GREATEST(last_activity_at, :at) WHERE question_id = :id
                """, new MapSqlParameterSource().addValue("id", questionId).addValue("delta", delta)
                .addValue("at", UtcTime.toDb(at)));
    }

    @Override
    public void adjustScore(long questionId, int delta) {
        jdbc.update("UPDATE dis_search_doc SET score = score + :delta WHERE question_id = :id",
                new MapSqlParameterSource().addValue("id", questionId).addValue("delta", delta));
    }

    @Override
    public void markAccepted(long questionId, Instant at) {
        jdbc.update("""
                UPDATE dis_search_doc SET accepted = TRUE, last_activity_at = GREATEST(last_activity_at, :at)
                WHERE question_id = :id
                """, new MapSqlParameterSource().addValue("id", questionId).addValue("at", UtcTime.toDb(at)));
    }

    @Override
    public void setBountyOpen(long questionId, boolean open) {
        jdbc.update("UPDATE dis_search_doc SET bounty_open = :open WHERE question_id = :id",
                new MapSqlParameterSource().addValue("id", questionId).addValue("open", open));
    }

    @Override
    public void markDeleted(long questionId) {
        jdbc.update("UPDATE dis_search_doc SET deleted = TRUE WHERE question_id = :id",
                new MapSqlParameterSource("id", questionId));
    }

    @Override
    public List<String> findTags(long questionId) {
        return jdbc.queryForList("SELECT tag FROM dis_question_tag WHERE question_id = :id ORDER BY tag",
                new MapSqlParameterSource("id", questionId), String.class);
    }

    @Override
    public List<RelatedCandidate> findCandidatesSharingTags(long questionId, List<String> tags, int limit) {
        return jdbc.query("""
                        SELECT d.question_id, d.title, d.tag_count, d.score, d.answer_count, d.accepted,
                               COUNT(*) AS shared
                        FROM dis_question_tag t JOIN dis_search_doc d ON d.question_id = t.question_id
                        WHERE t.tag IN (:tags) AND t.question_id <> :id AND d.deleted = FALSE
                        GROUP BY d.question_id, d.title, d.tag_count, d.score, d.answer_count, d.accepted
                        ORDER BY shared DESC, d.score DESC
                        LIMIT :limit
                        """, new MapSqlParameterSource().addValue("tags", tags).addValue("id", questionId)
                        .addValue("limit", limit),
                (rs, i) -> new RelatedCandidate(rs.getLong("question_id"), rs.getString("title"), rs.getInt("shared"),
                        rs.getInt("tag_count"), rs.getInt("score"), rs.getInt("answer_count"),
                        rs.getBoolean("accepted")));
    }

    private void replaceTags(long questionId, List<String> tags) {
        jdbc.update("DELETE FROM dis_question_tag WHERE question_id = :id", new MapSqlParameterSource("id", questionId));
        if (!tags.isEmpty()) {
            jdbc.batchUpdate("INSERT INTO dis_question_tag (question_id, tag) VALUES (:id, :tag)",
                    tags.stream().map(t -> new MapSqlParameterSource().addValue("id", questionId).addValue("tag", t))
                            .toArray(MapSqlParameterSource[]::new));
        }
    }
}
