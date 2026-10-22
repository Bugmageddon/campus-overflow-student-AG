package com.campusoverflow.reputation.application;

import com.campusoverflow.reputation.domain.LedgerRepository;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 适应度函数 FF-8：对账——每个账户的声誉必须等于其流水之和。 */
@Service
public class ReconciliationService {

    public record Mismatch(long userId, int accountReputation, long ledgerSum) {
    }

    private final ReputationAccountRepository accounts;
    private final LedgerRepository ledger;

    public ReconciliationService(ReputationAccountRepository accounts, LedgerRepository ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    @Transactional(readOnly = true)
    public List<Mismatch> reconcile() {
        // TODO(S3)：实现 ReconciliationService.reconcile——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：ReconciliationService.reconcile 尚未实现");
    }
}
