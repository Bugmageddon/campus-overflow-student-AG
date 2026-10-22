package com.campusoverflow.discovery.infrastructure.search;

import com.campusoverflow.discovery.domain.search.SearchQuery;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 退化实现：LIKE 模糊匹配（无相关度排序，数据量大时会全表扫描）。
 * 用于没有 ngram 的数据库，或作为课堂上“同一端口、两种适配器”的对照。启用：co.discovery.search.engine=like
 */
@Component
@ConditionalOnProperty(name = "co.discovery.search.engine", havingValue = "like")
public class LikeSearchAdapter extends AbstractJdbcSearchAdapter {

    public LikeSearchAdapter(NamedParameterJdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected void appendKeywordClause(SearchQuery q, StringBuilder where, MapSqlParameterSource p) {
        List<String> terms = q.terms();
        for (int i = 0; i < terms.size(); i++) {
            String name = "kw" + i;
            where.append(" AND (d.title LIKE :").append(name).append(" OR d.body_text LIKE :").append(name).append(")");
            p.addValue(name, "%" + terms.get(i).replace("%", "\\%").replace("_", "\\_") + "%");
        }
    }

    @Override
    protected String relevanceOrder(SearchQuery q) {
        return null;
    }
}
