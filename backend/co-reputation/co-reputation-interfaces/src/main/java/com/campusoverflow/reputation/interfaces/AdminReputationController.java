package com.campusoverflow.reputation.interfaces;

import com.campusoverflow.reputation.application.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理 Admin")
@RestController
@RequestMapping("/api/v1/admin/reputation")
public class AdminReputationController {

    private final ReconciliationService reconciliation;

    public AdminReputationController(ReconciliationService reconciliation) {
        this.reconciliation = reconciliation;
    }

    @Operation(summary = "立即执行声誉对账（FF-8）")
    @GetMapping("/reconciliation")
    public List<ReconciliationService.Mismatch> reconcile() {
        return reconciliation.reconcile();
    }
}
