package com.campusoverflow.qa.domain;

import java.util.Collection;

/** 标签使用计数（热门标签）。 */
public interface TagRepository {
    void adjustUsage(Collection<String> tagNames, int delta);
}
