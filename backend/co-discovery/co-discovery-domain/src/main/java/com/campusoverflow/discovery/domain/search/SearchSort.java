package com.campusoverflow.discovery.domain.search;

public enum SearchSort {
    /** 相关度（仅在有关键词时有意义，否则退化为 NEWEST）。 */
    RELEVANCE,
    NEWEST,
    /** 最近活跃（有新回答、被采纳）。 */
    ACTIVE,
    /** 得分最高。 */
    HOT
}
