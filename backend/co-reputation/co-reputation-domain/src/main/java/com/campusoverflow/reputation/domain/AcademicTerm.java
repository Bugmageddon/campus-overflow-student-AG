package com.campusoverflow.reputation.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.IsoFields;

/**
 * 排行榜周期键。学期划分（可按学校校历调整）：2–7 月为春季学期，8–12 月与次年 1 月为秋季学期。
 */
public final class AcademicTerm {

    public static final ZoneId CAMPUS_ZONE = ZoneId.of("Asia/Shanghai");

    private AcademicTerm() {
    }

    public static String termKey(Instant at) {
        // TODO(S3)：实现 AcademicTerm.termKey——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：AcademicTerm.termKey 尚未实现");
    }

    public static String weekKey(Instant at) {
        // TODO(S3)：实现 AcademicTerm.weekKey——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：AcademicTerm.weekKey 尚未实现");
    }

    public static String monthKey(Instant at) {
        // TODO(S3)：实现 AcademicTerm.monthKey——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：AcademicTerm.monthKey 尚未实现");
    }
}
