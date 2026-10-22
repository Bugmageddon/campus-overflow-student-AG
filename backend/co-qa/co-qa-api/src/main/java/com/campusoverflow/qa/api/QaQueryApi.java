package com.campusoverflow.qa.api;

import java.util.Optional;

/** Q&A 上下文对外的只读查询接口。 */
public interface QaQueryApi {
    Optional<QuestionRef> findQuestion(long questionId);
}
