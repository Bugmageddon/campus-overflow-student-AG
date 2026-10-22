package com.campusoverflow.shared.domain;

/**
 * 所有业务异常的基类。每个异常携带一个按上下文分段的错误码（ID-xxxx / QA-xxxx / REP-xxxx / DIS-xxxx），
 * 由 co-bootstrap 中的全局异常处理器统一映射为 RFC 9457 Problem Details。
 * <p>领域层只依赖本类，不依赖任何 HTTP 概念，从而保持与框架解耦。</p>
 */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }

    /** 建议映射的 HTTP 状态码（由接口层解释，领域层不关心其含义）。 */
    public abstract int suggestedStatus();
}
