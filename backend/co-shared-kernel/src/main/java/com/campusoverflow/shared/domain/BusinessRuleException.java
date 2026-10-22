package com.campusoverflow.shared.domain;

/** 违反业务规则或不变量，如标题过短、积分不足（映射为 422）。 */
public class BusinessRuleException extends DomainException {
    public BusinessRuleException(String code, String message) {
        super(code, message);
    }

    @Override
    public int suggestedStatus() {
        return 422;
    }
}
