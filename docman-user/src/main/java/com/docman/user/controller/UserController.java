package com.docman.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.docman.common.result.Result;
import com.docman.user.service.UserService;
import com.docman.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * User controller
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(userService.getCurrentUser(userId));
    }

    /**
     * Update current user info
     */
    @PutMapping("/me")
    public Result<UserVO> updateCurrentUser(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> request) {
        return Result.success(userService.updateUser(
                userId,
                request.get("displayName"),
                request.get("email"),
                request.get("avatarUrl")
        ));
    }

    /**
     * Change password
     */
    @PostMapping("/me/password")
    public Result<Void> changePassword(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> request) {
        userService.changePassword(userId, request.get("oldPassword"), request.get("newPassword"));
        return Result.success();
    }

    /**
     * List users (admin)
     */
    @GetMapping
    public Result<Page<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.listUsers(page, pageSize, keyword));
    }

    /**
     * Get user by ID (admin)
     */
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    /**
     * Delete user (admin)
     */
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return Result.success();
    }
}
