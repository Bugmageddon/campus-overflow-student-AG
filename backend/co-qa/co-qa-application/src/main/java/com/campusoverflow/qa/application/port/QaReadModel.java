package com.campusoverflow.qa.application.port;

import com.campusoverflow.qa.application.query.AnswerRow;
import com.campusoverflow.qa.application.query.CommentRow;
import com.campusoverflow.qa.application.query.QuestionRow;
import com.campusoverflow.qa.application.query.TagView;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 读模型端口（CQRS-lite）：查询直接返回投影行，绕过聚合装配，避免 N+1。
 * 所有查询默认排除已软删除的内容。
 */
public interface QaReadModel {
    PageResult<QuestionRow> listQuestions(Long courseId, PageRequest page);

    Optional<QuestionRow> findQuestion(long questionId);

    List<AnswerRow> findAnswers(long questionId);

    List<CommentRow> findComments(long questionId);

    /** 当前用户在该问题及其回答上的投票，key 形如 "QUESTION:10"、"ANSWER:50"。 */
    Map<String, Integer> findMyVotes(long voterId, long questionId, Collection<Long> answerIds);

    List<TagView> popularTags(int limit);
}
