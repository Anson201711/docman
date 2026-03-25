package com.docman.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.docman.common.result.Result;
import com.docman.user.dto.LoginRequest;
import com.docman.user.dto.LoginResponse;
import com.docman.user.dto.RegisterRequest;
import com.docman.user.service.UserService;
import com.docman.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Register new user
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    /**
     * Login
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request.getUsername(), request.getPassword()));
    }

    /**
     * Refresh token
     */
    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(request.get("userId"));
        String refreshToken = request.get("refreshToken");
        String newAccessToken = userService.refreshToken(userId, refreshToken);
        return Result.success(Map.of("accessToken", newAccessToken));
    }
}
