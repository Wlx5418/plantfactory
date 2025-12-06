package com.qzsf.plantfactoryspring.controller;

import com.qzsf.plantfactoryspring.dto.UserDTO;
import com.qzsf.plantfactoryspring.entity.User;
import com.qzsf.plantfactoryspring.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 提供用户CRUD操作的REST API接口
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@CrossOrigin
public class UserController {

    private final UserService userService;

    /**
     * 分页获取用户列表
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param username 用户名搜索（可选）
     * @param realName 真实姓名搜索（可选）
     * @param status 状态筛选（可选）
     * @return 分页用户列表
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'user:manage')")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getUserPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String status) {

        try {
            log.info("分页查询用户列表: pageNum={}, pageSize={}, username={}, realName={}, status={}",
                    pageNum, pageSize, username, realName, status);

            User.UserStatus userStatus = null;
            if (status != null && !status.isEmpty()) {
                try {
                    userStatus = User.UserStatus.valueOf(status);
                } catch (IllegalArgumentException e) {
                    log.warn("无效的用户状态参数: {}", status);
                }
            }

            Page<UserDTO> userPage = userService.getUserPage(pageNum, pageSize, username, realName, userStatus);

            return ResponseEntity.ok(ApiResponse.<Page<UserDTO>>builder()
                    .code(200)
                    .message("用户列表查询成功")
                    .data(userPage)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (Exception e) {
            log.error("分页查询用户列表失败: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<UserDTO>>builder()
                            .code(500)
                            .message("查询用户列表失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'user:manage')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long userId) {
        try {
            log.info("根据ID获取用户信息: userId={}", userId);

            UserDTO userDTO = userService.getUserById(userId);

            return ResponseEntity.ok(ApiResponse.<UserDTO>builder()
                    .code(200)
                    .message("获取用户信息成功")
                    .data(userDTO)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (EntityNotFoundException e) {
            log.warn("用户不存在: {}", userId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<UserDTO>builder()
                            .code(404)
                            .message("用户不存在")
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());

        } catch (Exception e) {
            log.error("获取用户信息失败: userId={}, error={}", userId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<UserDTO>builder()
                            .code(500)
                            .message("获取用户信息失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 创建新用户
     *
     * @param userCreateRequest 用户创建请求
     * @return 创建的用户信息
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'user:manage')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserService.UserCreateRequest userCreateRequest) {
        try {
            log.info("创建新用户: username={}, email={}", userCreateRequest.getUsername(), userCreateRequest.getEmail());

            UserDTO userDTO = userService.createUser(userCreateRequest);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<UserDTO>builder()
                            .code(201)
                            .message("用户创建成功")
                            .data(userDTO)
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .executionTime(0L)
                            .build());

        } catch (IllegalArgumentException e) {
            log.warn("创建用户失败 - 参数错误: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<UserDTO>builder()
                            .code(400)
                            .message(e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());

        } catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<UserDTO>builder()
                            .code(500)
                            .message("创建用户失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param userUpdateRequest 用户更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'user:manage')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserService.UserUpdateRequest userUpdateRequest) {

        try {
            log.info("更新用户信息: userId={}", userId);

            UserDTO userDTO = userService.updateUser(userId, userUpdateRequest);

            return ResponseEntity.ok(ApiResponse.<UserDTO>builder()
                    .code(200)
                    .message("用户信息更新成功")
                    .data(userDTO)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (EntityNotFoundException e) {
            log.warn("更新用户失败 - 用户不存在: {}", userId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<UserDTO>builder()
                            .code(404)
                            .message("用户不存在")
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());

        } catch (IllegalArgumentException e) {
            log.warn("更新用户失败 - 参数错误: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<UserDTO>builder()
                            .code(400)
                            .message(e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());

        } catch (Exception e) {
            log.error("更新用户信息失败: userId={}, error={}", userId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<UserDTO>builder()
                            .code(500)
                            .message("更新用户信息失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @param passwordResetRequest 密码重置请求
     * @return 操作结果
     */
    @PutMapping("/{userId}/reset-password")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'user:manage')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordResetRequest passwordResetRequest) {

        try {
            log.info("重置用户密码: userId={}", userId);

            userService.resetPassword(userId, passwordResetRequest.getNewPassword());

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(200)
                    .message("密码重置成功")
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (EntityNotFoundException e) {
            log.warn("重置密码失败 - 用户不存在: {}", userId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .code(404)
                            .message("用户不存在")
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());

        } catch (Exception e) {
            log.error("重置用户密码失败: userId={}, error={}", userId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .code(500)
                            .message("重置密码失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param statusUpdateRequest 状态更新请求
     * @return 操作结果
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'user:manage')")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody StatusUpdateRequest statusUpdateRequest) {

        try {
            log.info("更新用户状态: userId={}, status={}", userId, statusUpdateRequest.getStatus());

            User.UserStatus userStatus;
            try {
                userStatus = User.UserStatus.valueOf(statusUpdateRequest.getStatus());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的用户状态: " + statusUpdateRequest.getStatus());
            }

            userService.updateUserStatus(userId, userStatus);

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(200)
                    .message("用户状态更新成功")
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (EntityNotFoundException e) {
            log.warn("更新用户状态失败 - 用户不存在: {}", userId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .code(404)
                            .message("用户不存在")
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());

        } catch (IllegalArgumentException e) {
            log.warn("更新用户状态失败 - 参数错误: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .code(400)
                            .message(e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());

        } catch (Exception e) {
            log.error("更新用户状态失败: userId={}, error={}", userId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .code(500)
                            .message("更新用户状态失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'user:manage')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        try {
            log.info("删除用户: userId={}", userId);

            userService.deleteUser(userId);

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(200)
                    .message("用户删除成功")
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (EntityNotFoundException e) {
            log.warn("删除用户失败 - 用户不存在: {}", userId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .code(404)
                            .message("用户不存在")
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());

        } catch (Exception e) {
            log.error("删除用户失败: userId={}, error={}", userId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .code(500)
                            .message("删除用户失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 批量删除用户
     *
     * @param batchDeleteRequest 批量删除请求
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'user:manage')")
    public ResponseEntity<ApiResponse<Void>> batchDeleteUsers(@Valid @RequestBody BatchDeleteRequest batchDeleteRequest) {
        try {
            log.info("批量删除用户: count={}", batchDeleteRequest.getUserIds().size());

            userService.batchDeleteUsers(batchDeleteRequest.getUserIds());

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(200)
                    .message("批量删除用户成功")
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (Exception e) {
            log.error("批量删除用户失败: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .code(500)
                            .message("批量删除用户失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 获取用户状态选项
     *
     * @return 用户状态选项列表
     */
    @GetMapping("/status-options")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'user:manage')")
    public ResponseEntity<ApiResponse<List<UserService.UserStatusOption>>> getUserStatusOptions() {
        try {
            log.info("获取用户状态选项");

            List<UserService.UserStatusOption> statusOptions = userService.getUserStatusOptions();

            return ResponseEntity.ok(ApiResponse.<List<UserService.UserStatusOption>>builder()
                    .code(200)
                    .message("获取用户状态选项成功")
                    .data(statusOptions)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (Exception e) {
            log.error("获取用户状态选项失败: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<UserService.UserStatusOption>>builder()
                            .code(500)
                            .message("获取用户状态选项失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUserProfile() {
        try {
            log.info("获取当前用户信息");

            // 这里可以从SecurityContext获取当前用户信息
            // 为了简化，暂时返回空实现，实际项目中需要实现
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("message", "当前用户信息获取功能待实现");

            return ResponseEntity.ok(ApiResponse.<UserDTO>builder()
                    .code(200)
                    .message("获取当前用户信息成功")
                    .data(null)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (Exception e) {
            log.error("获取当前用户信息失败: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<UserDTO>builder()
                            .code(500)
                            .message("获取当前用户信息失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return "req_" + System.currentTimeMillis();
    }

    // 内部请求DTO类

    /**
     * 密码重置请求
     */
    public static class PasswordResetRequest {
        private String newPassword;

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    /**
     * 状态更新请求
     */
    public static class StatusUpdateRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /**
     * 批量删除请求
     */
    public static class BatchDeleteRequest {
        private List<Long> userIds;

        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
    }

    /**
     * 统一API响应格式
     */
    public static class ApiResponse<T> {
        private Integer code;
        private String message;
        private T data;
        private Instant timestamp;
        private String requestId;
        private Long executionTime;

        // Builder pattern
        public static class Builder<T> {
            private final ApiResponse<T> response = new ApiResponse<>();

            public Builder<T> code(Integer code) {
                response.code = code;
                return this;
            }

            public Builder<T> message(String message) {
                response.message = message;
                return this;
            }

            public Builder<T> data(T data) {
                response.data = data;
                return this;
            }

            public Builder<T> timestamp(Instant timestamp) {
                response.timestamp = timestamp;
                return this;
            }

            public Builder<T> requestId(String requestId) {
                response.requestId = requestId;
                return this;
            }

            public Builder<T> executionTime(Long executionTime) {
                response.executionTime = executionTime;
                return this;
            }

            public ApiResponse<T> build() {
                return response;
            }
        }

        public static <T> Builder<T> builder() {
            return new Builder<>();
        }

        // Getters
        public Integer getCode() { return code; }
        public String getMessage() { return message; }
        public T getData() { return data; }
        public Instant getTimestamp() { return timestamp; }
        public String getRequestId() { return requestId; }
        public Long getExecutionTime() { return executionTime; }
    }
}