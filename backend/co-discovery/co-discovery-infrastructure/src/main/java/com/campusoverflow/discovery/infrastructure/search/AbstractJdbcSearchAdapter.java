package com.campusoverflow.discovery.infrastructure.search;

import com.campusoverflow.discovery.domain.search.SearchHit;
import com.campusoverflow.discovery.domain.search.SearchPort;
import com.campusoverflow.discovery.domain.search.SearchQuery;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.util.UtcTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** 搜索适配器模板：过滤、排序、分页、标签回填是公共的；关键词匹配方式由子类决定。 */
abstract class AbstractJdbcSearchAdapter implements SearchPort {

    protected final NamedParameterJdbcTemplate jdbc;

    protected AbstractJdbcSearchAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 追加关键词过滤条件与参数。 */
    protected abstract void appendKeywordClause(SearchQuery q, StringBuilder where, MapSqlParameterSource p);

    /** 相关度排序表达式；不支持相关度时返回 null。 */
    protected abstract String relevanceOrder(SearchQuery q);

    @Override
    public PageResult<SearchHit> search(SearchQuery q) {
        StringBuilder where = new StringBuilder(" WHERE d.deleted = FALSE");
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (q.keyword() != null) {
            appendKeywordClause(q, where, p);
        }
        if (q.courseId() != null) {
            where.append(" AND d.course_id = :courseId");
            p.addValue("courseId", q.courseId());
        }
        if (q.tag() != null) {
            where.append(" AND EXISTS (SELECT 1 FROM dis_question_tag t WHERE t.question_id = d.question_id AND t.tag = :tag)");
            p.addValue("tag", q.tag());
        }
        if (q.unansweredOnly()) {
            where.append(" AND d.answer_count = 0");
        }
        if (q.bountyOnly()) {
            where.append(" AND d.bounty_open = TRUE");
        }
        String order = switch (q.sort()) {
            case RELEVANCE -> {
                String r = relevanceOrder(q);
                yield r == null ? "d.created_at DESC" : r + " DESC, d.created_at DESC";
            }
            case NEWEST -> "d.created_at DESC";
            case ACTIVE -> "d.last_activity_at DESC";
            case HOT -> "d.score DESC, d.answer_count DESC, d.created_at DESC";
        };
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM dis_search_doc d" + where, p, Long.class);
        p.addValue("limit", q.page().size()).addValue("offset", q.page().offset());
        List<SearchHit> hits = jdbc.query("""
                        SELECT d.question_id, d.title, LEFT(d.body_text, 200) AS snippet, d.course_id, d.author_id,
                               d.score, d.answer_count, d.accepted, d.bounty_open, d.created_at
                        FROM dis_search_doc d""" + where + " ORDER BY " + order + ", d.question_id DESC"
                        + " LIMIT :limit OFFSET :offset", p,
                (rs, i) -> {
                    long course = rs.getLong("course_id");
                    Long courseId = rs.wasNull() ? null : course;
                    return new SearchHit(rs.getLong("question_id"), rs.getString("title"), rs.getString("snippet"),
                            List.of(), courseId, rs.getLong("author_id"), rs.getInt("score"),
                            rs.getInt("answer_count"), rs.getBoolean("accepted"), rs.getBoolean("bounty_open"),
                            UtcTime.fromDb(rs.getObject("created_at", LocalDateTime.class)));
                });
        return new PageResult<>(withTags(hits), total == null ? 0 : total, q.page().page(), q.page().size());
    }

    private List<SearchHit> withTags(List<SearchHit> hits) {
        if (hits.isEmpty()) {
            return hits;
        }
        Map<Long, List<String>> tags = new HashMap<>();
        jdbc.query("SELECT question_id, tag FROM dis_question_tag WHERE question_id IN (:ids) ORDER BY tag",
                new MapSqlParameterSource("ids", hits.stream().map(SearchHit::questionId).toList()),
                rs -> {
                    tags.computeIfAbsent(rs.getLong("question_id"), k -> new ArrayList<>()).add(rs.getString("tag"));
                });
        return hits.stream().map(h -> new SearchHit(h.questionId(), h.title(), h.snippet(),
                tags.getOrDefault(h.questionId(), List.of()), h.courseId(), h.authorId(), h.score(), h.answerCount(),
                h.accepted(), h.bountyOpen(), h.createdAt())).toList();
    }
}
