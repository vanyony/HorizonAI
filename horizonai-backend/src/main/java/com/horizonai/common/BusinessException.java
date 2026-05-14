package com.horizonai.common;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    // ========== 手动 getter 方法（替代 Lombok @Getter） ==========

    public Integer getCode() { return code; }
}
