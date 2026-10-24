package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 问题的标签集合：去重后 1–5 个。 */
public record Tags(List<TagName> values) {

    public static final int MAX = 5;

    public Tags {
        Set<TagName> distinct = new LinkedHashSet<>(values);
        if (distinct.isEmpty() || distinct.size() > MAX) {
            throw new BusinessRuleException("QA-1005", "每个问题需要 1–" + MAX + " 个标签");
        }
        values = List.copyOf(distinct);
    }

    public static Tags of(Collection<String> raw) {
        if (raw == null) {
            throw new BusinessRuleException("QA-1005", "每个问题需要 1–" + MAX + " 个标签");
        }
        return new Tags(raw.stream().map(TagName::new).toList());
    }

    public List<String> names() {
        return values.stream().map(TagName::value).toList();
    }
}
