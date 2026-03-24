package com.docman.common.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;

import java.util.UUID;

/**
 * ID generation utility
 */
public class IdUtil {

    private IdUtil() {
    }

    /**
     * Generate simple ID (for display)
     */
    public static long nextId() {
        return snowflakeId();
    }

    /**
     * Generate snowflake ID
     */
    public static long snowflakeId() {
        return cn.hutool.core.util.IdUtil.getSnowflakeNextId();
    }

    /**
     * Generate UUID string
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generate UUID with dashes
     */
    public static String uuidWithDash() {
        return UUID.randomUUID().toString();
    }

    /**
     * Calculate SHA-256 hash
     */
    public static String sha256(String content) {
        return DigestUtil.sha256Hex(content);
    }

    /**
     * Calculate file hash (SHA-256)
     */
    public static String fileHash(byte[] content) {
        return DigestUtil.sha256Hex(content);
    }
}
