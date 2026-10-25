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
        LocalDate d = at.atZone(CAMPUS_ZONE).toLocalDate();
        int month = d.getMonthValue();
        if (month == 1) {
            return (d.getYear() - 1) + "-FALL";
        }
        if (month <= 7) {
            return d.getYear() + "-SPRING";
        }
        return d.getYear() + "-FALL";
    }

    public static String weekKey(Instant at) {
        LocalDate d = at.atZone(CAMPUS_ZONE).toLocalDate();
        return d.get(IsoFields.WEEK_BASED_YEAR) + "-W" + String.format("%02d", d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
    }

    public static String monthKey(Instant at) {
        LocalDate d = at.atZone(CAMPUS_ZONE).toLocalDate();
        return String.format("%d-%02d", d.getYear(), d.getMonthValue());
    }
}
