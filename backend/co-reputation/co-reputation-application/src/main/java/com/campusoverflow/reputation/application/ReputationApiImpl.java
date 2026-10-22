package com.campusoverflow.reputation.application;

import com.campusoverflow.reputation.api.ReputationApi;
import com.campusoverflow.reputation.api.ReputationSummary;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReputationApiImpl implements ReputationApi {

    private final ReputationAccountRepository accounts;

    public ReputationApiImpl(ReputationAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public ReputationSummary summary(long userId) {
        // TODO(S3)：实现 ReputationApiImpl.summary——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReputationApiImpl.summary 尚未实现");
    }

    @Override
    public Map<Long, Integer> reputations(Collection<Long> userIds) {
        // TODO(S3)：实现 ReputationApiImpl.reputations——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReputationApiImpl.reputations 尚未实现");
    }
}
