package com.docman.common.constant;

/**
 * System constants
 */
public class Constants {

    private Constants() {
    }

    // JWT
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";
    public static final String JWT_SECRET = "docman-jwt-secret-key-2024-enterprise-edition";
    public static final long JWT_EXPIRATION = 15 * 60 * 1000; // 15 minutes
    public static final long JWT_REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days

    // Redis
    public static final String REDIS_USER_PREFIX = "user:";
    public static final String REDIS_TOKEN_PREFIX = "token:";
    public static final String REDIS_PERMISSION_PREFIX = "permission:";
    public static final String REDIS_CATEGORY_TREE = "category:tree";
    public static final String REDIS_SEARCH_CACHE_PREFIX = "search:";
    public static final String REDIS_PRESIGNED_URL_PREFIX = "presigned:";

    // File
    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    public static final long MAX_STORAGE_QUOTA = 5L * 1024 * 1024 * 1024; // 5GB
    public static final String[] ALLOWED_FILE_TYPES = {
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".txt", ".rtf", ".odt", ".ods", ".odp",
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",
            ".zip", ".rar", ".7z",
            ".dwg", ".dxf", ".dwf", ".dgn",
            ".step", ".stp", ".iges", ".igs", ".obj", ".stl", ".3dxml", ".u3d", ".prt", ".asm",
            ".html", ".xml", ".json", ".md"
    };

    // Pagination
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Cache TTL (seconds)
    public static final long CACHE_TTL_SHORT = 5 * 60; // 5 minutes
    public static final long CACHE_TTL_MEDIUM = 30 * 60; // 30 minutes
    public static final long CACHE_TTL_LONG = 60 * 60; // 1 hour

    // Document Status
    public static final String DOC_STATUS_DRAFT = "DRAFT";
    public static final String DOC_STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String DOC_STATUS_APPROVED = "APPROVED";
    public static final String DOC_STATUS_REJECTED = "REJECTED";
    public static final String DOC_STATUS_ARCHIVED = "ARCHIVED";

    // Permission Levels
    public static final String PERM_READ = "READ";
    public static final String PERM_WRITE = "WRITE";
    public static final String PERM_DELETE = "DELETE";
    public static final String PERM_MANAGE = "MANAGE";

    // Approval Types
    public static final String APPROVAL_TYPE_SERIAL = "SERIAL";
    public static final String APPROVAL_TYPE_PARALLEL = "PARALLEL"; // 会签
    public static final String APPROVAL_TYPE_ANY = "ANY"; // 或签

    // Notification Types
    public static final String NOTIF_TYPE_IN_APP = "IN_APP";
    public static final String NOTIF_TYPE_EMAIL = "EMAIL";
    public static final String NOTIF_TYPE_BOTH = "BOTH";

    // Subscription Frequency
    public static final String SUB_FREQ_IMMEDIATE = "IMMEDIATE";
    public static final String SUB_FREQ_DAILY = "DAILY_DIGEST";
    public static final String SUB_FREQ_OFF = "OFF";
}
