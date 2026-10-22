package com.campusoverflow.reputation.infrastructure.jobs;

import com.campusoverflow.reputation.application.BountyService;
import com.campusoverflow.reputation.application.ReconciliationService;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 定时任务：悬赏到期处理；声誉对账（FF-8，结果以指标 co_reputation_mismatches 暴露给 Prometheus 告警）。 */
@Component
public class ReputationJobs {

    private static final Logger log = LoggerFactory.getLogger(ReputationJobs.class);

    private final BountyService bounties;
    private final ReconciliationService reconciliation;
    private final AtomicInteger mismatches;

    public ReputationJobs(BountyService bounties, ReconciliationService reconciliation, MeterRegistry registry) {
        this.bounties = bounties;
        this.reconciliation = reconciliation;
        this.mismatches = registry.gauge("co_reputation_mismatches", new AtomicInteger(0));
    }

    @Scheduled(fixedDelayString = "${co.reputation.bounty-expiry-interval-ms:300000}")
    public void expireBounties() {
        int n = bounties.expireDue();
        if (n > 0) {
            log.info("已关闭 {} 个到期悬赏并退回积分", n);
        }
    }

    @Scheduled(cron = "${co.reputation.reconcile-cron:0 30 3 * * *}", zone = "Asia/Shanghai")
    public void reconcile() {
        List<ReconciliationService.Mismatch> result = reconciliation.reconcile();
        mismatches.set(result.size());
        if (result.isEmpty()) {
            log.info("声誉对账通过：余额与流水一致");
        } else {
            log.error("声誉对账发现 {} 个不一致账户: {}", result.size(), result);
        }
    }
}
