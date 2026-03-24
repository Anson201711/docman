package com.docman.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User view object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    private Long id;

    private String username;

    private String email;

    private String displayName;

    private String avatarUrl;

    private String role;

    private Long storageQuota;

    private Long storageUsed;

    private String lastLoginIp;

    private String lastLoginAt;
}
