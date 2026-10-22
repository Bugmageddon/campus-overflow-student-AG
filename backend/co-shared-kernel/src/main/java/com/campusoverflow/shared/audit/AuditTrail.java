package com.campusoverflow.shared.audit;

/** 审计端口：登录、角色变更、内容删除、悬赏结算等敏感操作写入只追加的审计表。 */
public interface AuditTrail {
    void record(String action, Long actorId, String targetType, String targetId, String detail);
}
