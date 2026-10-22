package com.campusoverflow.qa.application;

import com.campusoverflow.qa.api.QaQueryApi;
import com.campusoverflow.qa.api.QuestionRef;
import com.campusoverflow.qa.domain.QuestionRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QaQueryApiImpl implements QaQueryApi {

    private final QuestionRepository questions;

    public QaQueryApiImpl(QuestionRepository questions) {
        this.questions = questions;
    }

    @Override
    public Optional<QuestionRef> findQuestion(long questionId) {
        // TODO(S2)：实现 QaQueryApiImpl.findQuestion——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S2)：QaQueryApiImpl.findQuestion 尚未实现");
    }
}
