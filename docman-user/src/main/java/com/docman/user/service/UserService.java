package com.docman.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.docman.common.constant.Constants;
import com.docman.common.exception.BusinessException;
import com.docman.common.result.ResultCode;
import com.docman.common.util.JwtUtil;
import com.docman.user.dto.LoginResponse;
import com.docman.user.dto.RegisterRequest;
import com.docman.user.entity.User;
import com.docman.user.repository.UserMapper;
import com.docman.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * User service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    /**
     * Register new user
     */
    public UserVO register(RegisterRequest request) {
        // Check if username exists
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())) > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), "Username already exists");
        }

        // Check if email exists
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail())) > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), "Email already exists");
        }

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername());
        user.setStatus(1);
        user.setRole("USER");
        user.setStorageQuota(Constants.MAX_STORAGE_QUOTA);
        user.setStorageUsed(0L);

        userMapper.insert(user);

        log.info("User registered: {}", user.getUsername());

        return toUserVO(user);
    }

    /**
     * Login
     */
    public LoginResponse login(String username, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (user == null) {
            throw new BusinessException(ResultCode.INVALID_CREDENTIALS.getCode(), "Invalid credentials");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "User account is disabled");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ResultCode.INVALID_CREDENTIALS.getCode(), "Invalid credentials");
        }

        // Generate tokens
        String accessToken = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = JwtUtil.generateRefreshToken(user.getId());

        // Store refresh token in Redis
        redisTemplate.opsForValue().set(
                Constants.REDIS_TOKEN_PREFIX + user.getId(),
                refreshToken,
                Constants.JWT_REFRESH_EXPIRATION,
                TimeUnit.MILLISECONDS
        );

        // Update last login
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userMapper.updateById(user);

        log.info("User logged in: {}", username);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(Constants.JWT_EXPIRATION / 1000)
                .user(toUserVO(user))
                .build();
    }

    /**
     * Refresh access token
     */
    public String refreshToken(Long userId, String refreshToken) {
        // Verify refresh token from Redis
        String storedToken = (String) redisTemplate.opsForValue().get(Constants.REDIS_TOKEN_PREFIX + userId);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID.getCode(), "Invalid refresh token");
        }

        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "User not found or disabled");
        }

        return JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    /**
     * Get user by ID
     */
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "User not found");
        }
        return toUserVO(user);
    }

    /**
     * Get current user from token
     */
    public UserVO getCurrentUser(Long userId) {
        return getUserById(userId);
    }

    /**
     * List users (admin)
     */
    public Page<UserVO> listUsers(int page, int pageSize, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword)
                    .or()
                    .like(User::getDisplayName, keyword);
        }

        Page<User> userPage = userMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Page<UserVO> resultPage = new Page<>(page, pageSize, userPage.getTotal());
        resultPage.setRecords(userPage.getRecords().stream().map(this::toUserVO).toList());

        return resultPage;
    }

    /**
     * Update user
     */
    public UserVO updateUser(Long userId, String displayName, String email, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "User not found");
        }

        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }

        userMapper.updateById(user);

        return toUserVO(user);
    }

    /**
     * Delete user (admin)
     */
    public void deleteUser(Long userId) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "User not found");
        }
        userMapper.deleteById(userId);
        log.info("User deleted: {}", userId);
    }

    /**
     * Change password
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "User not found");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException(ResultCode.INVALID_CREDENTIALS.getCode(), "Incorrect old password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("Password changed for user: {}", userId);
    }

    /**
     * Convert User to UserVO
     */
    private UserVO toUserVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .storageQuota(user.getStorageQuota())
                .storageUsed(user.getStorageUsed())
                .lastLoginIp(user.getLastLoginIp())
                .lastLoginAt(user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null)
                .build();
    }
}
