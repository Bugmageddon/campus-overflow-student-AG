package com.campusoverflow.qa.infrastructure.persistence;

import com.campusoverflow.qa.domain.TagRepository;
import java.util.Collection;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** 标签使用计数：UPSERT 原子累加（MySQL ON DUPLICATE KEY UPDATE）。 */
@Repository
public class JdbcTagRepository implements TagRepository {

    private static final String UPSERT = """
            INSERT INTO qa_tag (name, question_count) VALUES (:name, GREATEST(:delta, 0))
            ON DUPLICATE KEY UPDATE question_count = GREATEST(question_count + :delta, 0)
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcTagRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void adjustUsage(Collection<String> tagNames, int delta) {
        if (tagNames == null || tagNames.isEmpty() || delta == 0) {
            return;
        }
        MapSqlParameterSource[] batch = tagNames.stream()
                .map(n -> new MapSqlParameterSource().addValue("name", n).addValue("delta", delta))
                .toArray(MapSqlParameterSource[]::new);
        jdbc.batchUpdate(UPSERT, batch);
    }
}
