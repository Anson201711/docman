package com.docman.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Storage Quota Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageQuotaResponse {

    /**
     * User ID
     */
    private String userId;

    /**
     * Maximum quota in bytes
     */
    private Long maxQuota;

    /**
     * Used quota in bytes
     */
    private Long usedQuota;

    /**
     * Available quota in bytes
     */
    private Long availableQuota;

    /**
     * Usage percentage
     */
    private Double usagePercentage;

    /**
     * Quota active status
     */
    private Boolean active;
}
