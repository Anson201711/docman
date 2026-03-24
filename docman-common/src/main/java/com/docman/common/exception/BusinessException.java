package com.docman.common.exception;

import lombok.Getter;

/**
 * Business exception with code
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public static BusinessException of(int code, String message) {
        return new BusinessException(code, message);
    }
}
