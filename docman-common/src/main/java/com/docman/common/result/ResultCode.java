package com.docman.common.result;

import lombok.Getter;

/**
 * Result codes enumeration
 */
@Getter
public enum ResultCode {

    // Success codes (2xxx)
    SUCCESS(200, "Operation successful"),
    CREATED(201, "Resource created successfully"),
    DELETED(204, "Resource deleted successfully"),

    // Client error codes (4xxx)
    FAIL(400, "Operation failed"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Resource not found"),
    METHOD_NOT_ALLOWED(405, "Method not allowed"),
    CONFLICT(409, "Resource conflict"),
    VALIDATION_ERROR(422, "Validation error"),

    // Server error codes (5xxx)
    INTERNAL_ERROR(500, "Internal server error"),
    SERVICE_UNAVAILABLE(503, "Service unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway timeout"),

    // Business error codes (6xxx)
    USER_NOT_FOUND(6001, "User not found"),
    USER_ALREADY_EXISTS(6002, "User already exists"),
    INVALID_CREDENTIALS(6003, "Invalid credentials"),
    TOKEN_EXPIRED(6004, "Token expired"),
    TOKEN_INVALID(6005, "Token invalid"),
    PERMISSION_DENIED(6006, "Permission denied"),

    DOCUMENT_NOT_FOUND(7001, "Document not found"),
    FILE_TOO_LARGE(7002, "File too large"),
    UNSUPPORTED_FILE_TYPE(7003, "Unsupported file type"),
    STORAGE_QUOTA_EXCEEDED(7004, "Storage quota exceeded"),

    CATEGORY_NOT_FOUND(8001, "Category not found"),
    CATEGORY_HAS_CHILDREN(8002, "Category has children"),

    APPROVAL_NOT_FOUND(9001, "Approval not found"),
    APPROVAL_IN_PROGRESS(9002, "Approval in progress"),
    APPROVAL_ALREADY_COMPLETED(9003, "Approval already completed");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
