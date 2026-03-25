package com.docman.user.dto;

import com.docman.user.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login response with tokens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;

    private String refreshToken;

    private Long expiresIn;

    private UserVO user;
}
