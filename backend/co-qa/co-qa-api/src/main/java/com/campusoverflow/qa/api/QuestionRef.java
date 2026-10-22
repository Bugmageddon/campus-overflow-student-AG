package com.campusoverflow.qa.api;

/** 问题引用（发布语言）：其他上下文需要的最少信息。 */
public record QuestionRef(long id, long authorId, Long courseId, String title, Long acceptedAnswerId,
                          boolean deleted) {
}
