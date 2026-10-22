package com.campusoverflow.shared.domain;

/** 与当前资源状态冲突，如重复注册、重复采纳（映射为 409）。 */
public class ConflictException extends DomainException {
    public ConflictException(String code, String message) {
        super(code, message);
    }

    @Override
    public int suggestedStatus() {
        return 409;
    }
}
