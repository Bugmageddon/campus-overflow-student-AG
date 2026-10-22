package com.campusoverflow.shared.domain;

/** 当前操作者无权执行该操作（映射为 403）。领域级授权失败时抛出。 */
public class ForbiddenException extends DomainException {
    public ForbiddenException(String code, String message) {
        super(code, message);
    }

    @Override
    public int suggestedStatus() {
        return 403;
    }
}
