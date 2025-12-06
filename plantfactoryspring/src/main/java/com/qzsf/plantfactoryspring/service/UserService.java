package com.qzsf.plantfactoryspring.service;

import com.qzsf.plantfactoryspring.dto.UserDTO;
import com.qzsf.plantfactoryspring.entity.Role;
import com.qzsf.plantfactoryspring.entity.User;
import com.qzsf.plantfactoryspring.repository.RoleRepository;
import com.qzsf.plantfactoryspring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户管理服务
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param username 用户名搜索（可选）
     * @param realName 真实姓名搜索（可选）
     * @param status 状态筛选（可选）
     * @return 分页用户列表
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getUserPage(int pageNum, int pageSize, String username, String realName, User.UserStatus status) {
        log.info("分页查询用户列表: pageNum={}, pageSize={}, username={}, realName={}, status={}",
                pageNum, pageSize, username, realName, status);

        // 创建分页和排序条件
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);

        // 执行分页查询
        Page<User> userPage = userRepository.findUsersWithFilters(username, realName, status, pageable);

        // 转换为DTO
        return userPage.map(UserDTO::fromEntity);
    }

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        log.info("根据ID获取用户信息: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + userId));

        return UserDTO.fromEntity(user);
    }

    /**
     * 创建用户
     *
     * @param userCreateRequest 用户创建请求
     * @return 创建的用户信息
     */
    public UserDTO createUser(UserCreateRequest userCreateRequest) {
        log.info("创建用户: username={}, email={}", userCreateRequest.getUsername(), userCreateRequest.getEmail());

        // 检查用户名是否已存在
        if (userRepository.existsByUsernameAndIsDeletedFalse(userCreateRequest.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + userCreateRequest.getUsername());
        }

        // 检查邮箱是否已存在
        if (userCreateRequest.getEmail() != null &&
            userRepository.existsByEmailAndIsDeletedFalse(userCreateRequest.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在: " + userCreateRequest.getEmail());
        }

        // 获取角色信息
        Set<Role> roles = new HashSet<>();
        if (userCreateRequest.getRoleIds() != null && !userCreateRequest.getRoleIds().isEmpty()) {
            roles = roleRepository.findAllById(userCreateRequest.getRoleIds()).stream()
                    .collect(Collectors.toSet());
        }

        // 创建用户
        User user = User.builder()
                .username(userCreateRequest.getUsername())
                .password(passwordEncoder.encode(userCreateRequest.getPassword()))
                .email(userCreateRequest.getEmail())
                .phone(userCreateRequest.getPhone())
                .realName(userCreateRequest.getRealName())
                .avatarUrl(userCreateRequest.getAvatarUrl())
                .status(userCreateRequest.getStatus() != null ? userCreateRequest.getStatus() : User.UserStatus.ACTIVE)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        log.info("用户创建成功: userId={}", savedUser.getId());

        return UserDTO.fromEntity(savedUser);
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param userUpdateRequest 用户更新请求
     * @return 更新后的用户信息
     */
    public UserDTO updateUser(Long userId, UserUpdateRequest userUpdateRequest) {
        log.info("更新用户信息: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + userId));

        // 检查用户名是否被其他用户使用
        if (userUpdateRequest.getUsername() != null &&
            !userUpdateRequest.getUsername().equals(user.getUsername()) &&
            userRepository.existsByUsernameAndIsDeletedFalse(userUpdateRequest.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + userUpdateRequest.getUsername());
        }

        // 检查邮箱是否被其他用户使用
        if (userUpdateRequest.getEmail() != null &&
            !userUpdateRequest.getEmail().equals(user.getEmail()) &&
            userRepository.existsByEmailAndIsDeletedFalse(userUpdateRequest.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在: " + userUpdateRequest.getEmail());
        }

        // 更新用户信息
        if (userUpdateRequest.getUsername() != null) {
            user.setUsername(userUpdateRequest.getUsername());
        }
        if (userUpdateRequest.getEmail() != null) {
            user.setEmail(userUpdateRequest.getEmail());
        }
        if (userUpdateRequest.getPhone() != null) {
            user.setPhone(userUpdateRequest.getPhone());
        }
        if (userUpdateRequest.getRealName() != null) {
            user.setRealName(userUpdateRequest.getRealName());
        }
        if (userUpdateRequest.getAvatarUrl() != null) {
            user.setAvatarUrl(userUpdateRequest.getAvatarUrl());
        }
        if (userUpdateRequest.getStatus() != null) {
            user.setStatus(userUpdateRequest.getStatus());
        }

        // 更新角色
        if (userUpdateRequest.getRoleIds() != null) {
            Set<Role> roles = roleRepository.findAllById(userUpdateRequest.getRoleIds()).stream()
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        log.info("用户信息更新成功: userId={}", userId);

        return UserDTO.fromEntity(updatedUser);
    }

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    public void resetPassword(Long userId, String newPassword) {
        log.info("重置用户密码: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("用户密码重置成功: userId={}", userId);
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 新状态
     */
    public void updateUserStatus(Long userId, User.UserStatus status) {
        log.info("更新用户状态: userId={}, status={}", userId, status);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + userId));

        user.setStatus(status);
        userRepository.save(user);

        log.info("用户状态更新成功: userId={}, status={}", userId, status);
    }

    /**
     * 删除用户（软删除）
     *
     * @param userId 用户ID
     */
    public void deleteUser(Long userId) {
        log.info("删除用户: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + userId));

        userRepository.softDeleteUser(userId, LocalDateTime.now());

        log.info("用户删除成功: userId={}", userId);
    }

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID列表
     */
    public void batchDeleteUsers(List<Long> userIds) {
        log.info("批量删除用户: userIds={}", userIds);

        LocalDateTime now = LocalDateTime.now();
        for (Long userId : userIds) {
            userRepository.softDeleteUser(userId, now);
        }

        log.info("批量删除用户成功: count={}", userIds.size());
    }

    /**
     * 获取所有用户状态选项
     *
     * @return 用户状态列表
     */
    @Transactional(readOnly = true)
    public List<UserStatusOption> getUserStatusOptions() {
        return List.of(
            new UserStatusOption(User.UserStatus.ACTIVE.getCode(), User.UserStatus.ACTIVE.getDescription()),
            new UserStatusOption(User.UserStatus.INACTIVE.getCode(), User.UserStatus.INACTIVE.getDescription()),
            new UserStatusOption(User.UserStatus.LOCKED.getCode(), User.UserStatus.LOCKED.getDescription())
        );
    }

    // 内部请求DTO类

    /**
     * 用户创建请求
     */
    public static class UserCreateRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
        private String realName;
        private String avatarUrl;
        private User.UserStatus status;
        private Set<Long> roleIds;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

        public User.UserStatus getStatus() { return status; }
        public void setStatus(User.UserStatus status) { this.status = status; }

        public Set<Long> getRoleIds() { return roleIds; }
        public void setRoleIds(Set<Long> roleIds) { this.roleIds = roleIds; }
    }

    /**
     * 用户更新请求
     */
    public static class UserUpdateRequest {
        private String username;
        private String email;
        private String phone;
        private String realName;
        private String avatarUrl;
        private User.UserStatus status;
        private Set<Long> roleIds;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

        public User.UserStatus getStatus() { return status; }
        public void setStatus(User.UserStatus status) { this.status = status; }

        public Set<Long> getRoleIds() { return roleIds; }
        public void setRoleIds(Set<Long> roleIds) { this.roleIds = roleIds; }
    }

    /**
     * 用户状态选项
     */
    public static class UserStatusOption {
        private String code;
        private String description;

        public UserStatusOption(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }
}