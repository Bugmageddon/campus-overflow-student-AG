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
        Map<Long, Long> sums = ledger.sumByUser();
        List<Mismatch> mismatches = new ArrayList<>();
        for (ReputationAccount account : accounts.findAll()) {
            long ledgerSum = sums.getOrDefault(account.userId(), 0L);
            if (account.reputation() != ledgerSum) {
                mismatches.add(new Mismatch(account.userId(), account.reputation(), ledgerSum));
            }
        }
        return mismatches;
    }
}
