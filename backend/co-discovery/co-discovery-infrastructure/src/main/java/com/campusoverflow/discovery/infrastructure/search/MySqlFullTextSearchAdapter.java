package com.campusoverflow.discovery.infrastructure.search;

import com.campusoverflow.discovery.domain.search.SearchQuery;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * ADR-005：MySQL 8 FULLTEXT + ngram 分词器（默认解析器按空格分词，无法检索中文）。
 * 每个词项转为必须出现的短语：+"红黑树" +"旋转"。
 */
@Component
@ConditionalOnProperty(name = "co.discovery.search.engine", havingValue = "fulltext", matchIfMissing = true)
public class MySqlFullTextSearchAdapter extends AbstractJdbcSearchAdapter {

    private static final String MATCH = "MATCH(d.title, d.body_text) AGAINST (:kw IN BOOLEAN MODE)";

    public MySqlFullTextSearchAdapter(NamedParameterJdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected void appendKeywordClause(SearchQuery q, StringBuilder where, MapSqlParameterSource p) {
        where.append(" AND ").append(MATCH);
        p.addValue("kw", q.terms().stream().map(t -> "+\"" + t + "\"").collect(Collectors.joining(" ")));
    }

    @Override
    protected String relevanceOrder(SearchQuery q) {
        return MATCH;
    }
}
