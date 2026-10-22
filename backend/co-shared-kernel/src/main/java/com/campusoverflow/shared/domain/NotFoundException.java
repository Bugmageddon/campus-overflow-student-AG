package com.campusoverflow.shared.domain;

/** 资源不存在（映射为 404）。 */
public class NotFoundException extends DomainException {
    public NotFoundException(String code, String message) {
        super(code, message);
    }

    @Override
    public int suggestedStatus() {
        return 404;
    }
}
