package com.qzsf.plantfactoryspring.controller;

import com.qzsf.plantfactoryspring.entity.Role;
import com.qzsf.plantfactoryspring.entity.Permission;
import com.qzsf.plantfactoryspring.repository.RoleRepository;
import com.qzsf.plantfactoryspring.repository.PermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 * 提供角色CRUD操作的REST API接口
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Validated
@CrossOrigin
public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * 分页获取角色列表
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param name 角色名称搜索（可选）
     * @param status 状态筛选（可选）
     * @return 分页角色列表
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'role:manage')")
    public ResponseEntity<ApiResponse<Page<RoleDTO>>> getRolePage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {

        try {
            log.info("分页查询角色列表: page={}, size={}, name={}, status={}",
                    page, size, name, status);

            // 创建分页和排序
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Role> rolePage;
            if (name != null && !name.isEmpty()) {
                if (status != null && !status.isEmpty()) {
                    try {
                        Role.RoleStatus roleStatus = Role.RoleStatus.valueOf(status);
                        rolePage = roleRepository.findByRoleNameContainingIgnoreCaseAndStatus(name, roleStatus, pageable);
                    } catch (IllegalArgumentException e) {
                        rolePage = roleRepository.findByRoleNameContainingIgnoreCase(name, pageable);
                    }
                } else {
                    rolePage = roleRepository.findByRoleNameContainingIgnoreCase(name, pageable);
                }
            } else if (status != null && !status.isEmpty()) {
                try {
                    Role.RoleStatus roleStatus = Role.RoleStatus.valueOf(status);
                    rolePage = roleRepository.findByStatus(roleStatus, pageable);
                } catch (IllegalArgumentException e) {
                    rolePage = roleRepository.findAll(pageable);
                }
            } else {
                rolePage = roleRepository.findAll(pageable);
            }

            // 转换为DTO
            Page<RoleDTO> roleDTOPage = rolePage.map(this::convertToRoleDTO);

            return ResponseEntity.ok(ApiResponse.<Page<RoleDTO>>builder()
                    .code(200)
                    .message("角色列表查询成功")
                    .data(roleDTOPage)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (Exception e) {
            log.error("分页查询角色列表失败: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<RoleDTO>>builder()
                            .code(500)
                            .message("查询角色列表失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 根据ID获取角色信息
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'role:manage')")
    public ResponseEntity<ApiResponse<RoleDTO>> getRoleById(@PathVariable Long roleId) {
        try {
            log.info("根据ID获取角色信息: roleId={}", roleId);

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new EntityNotFoundException("角色不存在"));

            RoleDTO roleDTO = convertToRoleDTO(role);

            return ResponseEntity.ok(ApiResponse.<RoleDTO>builder()
                    .code(200)
                    .message("获取角色信息成功")
                    .data(roleDTO)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (EntityNotFoundException e) {
            log.warn("角色不存在: {}", roleId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<RoleDTO>builder()
                            .code(404)
                            .message("角色不存在")
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());

        } catch (Exception e) {
            log.error("获取角色信息失败: roleId={}, error={}", roleId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RoleDTO>builder()
                            .code(500)
                            .message("获取角色信息失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 创建新角色
     *
     * @param roleCreateRequest 角色创建请求
     * @return 创建的角色信息
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'role:manage')")
    @Transactional
    public ResponseEntity<ApiResponse<RoleDTO>> createRole(@Valid @RequestBody RoleCreateRequest roleCreateRequest) {
        try {
            log.info("创建新角色: roleName={}, roleCode={}", roleCreateRequest.getRoleName(), roleCreateRequest.getRoleCode());

            // 检查角色名称和编码是否已存在
            if (roleRepository.existsByRoleName(roleCreateRequest.getRoleName())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<RoleDTO>builder()
                                .code(400)
                                .message("角色名称已存在")
                                .timestamp(Instant.now())
                                .requestId(generateRequestId())
                                .build());
            }

            if (roleRepository.existsByRoleCode(roleCreateRequest.getRoleCode())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<RoleDTO>builder()
                                .code(400)
                                .message("角色编码已存在")
                                .timestamp(Instant.now())
                                .requestId(generateRequestId())
                                .build());
            }

            // 创建角色
            Role role = Role.builder()
                    .roleName(roleCreateRequest.getRoleName())
                    .roleCode(roleCreateRequest.getRoleCode())
                    .description(roleCreateRequest.getDescription())
                    .status(roleCreateRequest.getStatus())
                    .build();

            // 设置权限
            if (roleCreateRequest.getPermissionIds() != null && !roleCreateRequest.getPermissionIds().isEmpty()) {
                Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(roleCreateRequest.getPermissionIds()));
                role.setPermissions(permissions);
            }

            role = roleRepository.save(role);
            RoleDTO roleDTO = convertToRoleDTO(role);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<RoleDTO>builder()
                            .code(201)
                            .message("角色创建成功")
                            .data(roleDTO)
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .executionTime(0L)
                            .build());

        } catch (Exception e) {
            log.error("创建角色失败: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RoleDTO>builder()
                            .code(500)
                            .message("创建角色失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 更新角色信息
     *
     * @param roleId 角色ID
     * @param roleUpdateRequest 角色更新请求
     * @return 更新后的角色信息
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'role:manage')")
    @Transactional
    public ResponseEntity<ApiResponse<RoleDTO>> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleUpdateRequest roleUpdateRequest) {

        try {
            log.info("更新角色信息: roleId={}", roleId);

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new EntityNotFoundException("角色不存在"));

            // 检查角色名称是否与其他角色冲突
            if (!role.getRoleName().equals(roleUpdateRequest.getRoleName()) &&
                    roleRepository.existsByRoleName(roleUpdateRequest.getRoleName())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<RoleDTO>builder()
                                .code(400)
                                .message("角色名称已存在")
                                .timestamp(Instant.now())
                                .requestId(generateRequestId())
                                .build());
            }

            // 更新角色信息
            role.setRoleName(roleUpdateRequest.getRoleName());
            role.setDescription(roleUpdateRequest.getDescription());
            role.setStatus(roleUpdateRequest.getStatus());

            // 更新权限
            if (roleUpdateRequest.getPermissionIds() != null) {
                Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(roleUpdateRequest.getPermissionIds()));
                role.setPermissions(permissions);
            }

            role = roleRepository.save(role);
            RoleDTO roleDTO = convertToRoleDTO(role);

            return ResponseEntity.ok(ApiResponse.<RoleDTO>builder()
                    .code(200)
                    .message("角色信息更新成功")
                    .data(roleDTO)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (EntityNotFoundException e) {
            log.warn("更新角色失败 - 角色不存在: {}", roleId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<RoleDTO>builder()
                            .code(404)
                            .message("角色不存在")
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());

        } catch (Exception e) {
            log.error("更新角色信息失败: roleId={}, error={}", roleId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RoleDTO>builder()
                            .code(500)
                            .message("更新角色信息失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 操作结果
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'role:manage')")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long roleId) {
        try {
            log.info("删除角色: roleId={}", roleId);

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new EntityNotFoundException("角色不存在"));

            roleRepository.delete(role);

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(200)
                    .message("角色删除成功")
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (EntityNotFoundException e) {
            log.warn("删除角色失败 - 角色不存在: {}", roleId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .code(404)
                            .message("角色不存在")
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());

        } catch (Exception e) {
            log.error("删除角色失败: roleId={}, error={}", roleId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .code(500)
                            .message("删除角色失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 更新角色状态
     *
     * @param roleId 角色ID
     * @param statusUpdateRequest 状态更新请求
     * @return 操作结果
     */
    @PutMapping("/{roleId}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'role:manage')")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> updateRoleStatus(
            @PathVariable Long roleId,
            @Valid @RequestBody StatusUpdateRequest statusUpdateRequest) {

        try {
            log.info("更新角色状态: roleId={}, status={}", roleId, statusUpdateRequest.getStatus());

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new EntityNotFoundException("角色不存在"));

            Role.RoleStatus roleStatus;
            try {
                roleStatus = Role.RoleStatus.valueOf(statusUpdateRequest.getStatus());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的角色状态: " + statusUpdateRequest.getStatus());
            }

            role.setStatus(roleStatus);
            roleRepository.save(role);

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(200)
                    .message("角色状态更新成功")
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (EntityNotFoundException e) {
            log.warn("更新角色状态失败 - 角色不存在: {}", roleId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .code(404)
                            .message("角色不存在")
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());

        } catch (IllegalArgumentException e) {
            log.warn("更新角色状态失败 - 参数错误: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .code(400)
                            .message(e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());

        } catch (Exception e) {
            log.error("更新角色状态失败: roleId={}, error={}", roleId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .code(500)
                            .message("更新角色状态失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 获取所有权限选项
     *
     * @return 权限选项列表
     */
    @GetMapping("/permissions/options")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'role:manage')")
    public ResponseEntity<ApiResponse<List<PermissionOptionDTO>>> getPermissionOptions() {
        try {
            log.info("获取权限选项");

            List<Permission> permissions = permissionRepository.findAll(Sort.by("sortOrder", "name"));

            List<PermissionOptionDTO> permissionOptions = permissions.stream()
                    .map(permission -> PermissionOptionDTO.builder()
                            .value(permission.getCode())
                            .label(permission.getName())
                            .description(permission.getDescription())
                            .resourceType(permission.getResourceType().getCode())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.<List<PermissionOptionDTO>>builder()
                    .code(200)
                    .message("获取权限选项成功")
                    .data(permissionOptions)
                    .timestamp(Instant.now())
                    .requestId(generateRequestId())
                    .executionTime(0L)
                    .build());

        } catch (Exception e) {
            log.error("获取权限选项失败: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<PermissionOptionDTO>>builder()
                            .code(500)
                            .message("获取权限选项失败: " + e.getMessage())
                            .timestamp(Instant.now())
                            .requestId(generateRequestId())
                            .build());
        }
    }

    /**
     * 转换为角色DTO
     */
    private RoleDTO convertToRoleDTO(Role role) {
        List<String> permissionNames = role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toList());

        return RoleDTO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .description(role.getDescription())
                .status(role.getStatus().getCode())
                .permissions(permissionNames)
                .userCount(0L) // TODO: 实现用户数量统计
                .createdAt(role.getCreatedAt().toInstant(java.time.ZoneOffset.UTC))
                .updatedAt(role.getUpdatedAt().toInstant(java.time.ZoneOffset.UTC))
                .build();
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return "req_" + System.currentTimeMillis();
    }

    // 内部请求DTO类

    /**
     * 角色创建请求
     */
    public static class RoleCreateRequest {
        private String roleName;
        private String roleCode;
        private String description;
        private Role.RoleStatus status = Role.RoleStatus.ACTIVE;
        private List<Long> permissionIds = new ArrayList<>();

        // Getters and Setters
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
        public String getRoleCode() { return roleCode; }
        public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Role.RoleStatus getStatus() { return status; }
        public void setStatus(Role.RoleStatus status) { this.status = status; }
        public List<Long> getPermissionIds() { return permissionIds; }
        public void setPermissionIds(List<Long> permissionIds) { this.permissionIds = permissionIds; }
    }

    /**
     * 角色更新请求
     */
    public static class RoleUpdateRequest {
        private String roleName;
        private String description;
        private Role.RoleStatus status;
        private List<Long> permissionIds;

        // Getters and Setters
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Role.RoleStatus getStatus() { return status; }
        public void setStatus(Role.RoleStatus status) { this.status = status; }
        public List<Long> getPermissionIds() { return permissionIds; }
        public void setPermissionIds(List<Long> permissionIds) { this.permissionIds = permissionIds; }
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

    /**
     * 角色DTO
     */
    public static class RoleDTO {
        private Long id;
        private String roleName;
        private String roleCode;
        private String description;
        private String status;
        private List<String> permissions;
        private Long userCount;
        private Instant createdAt;
        private Instant updatedAt;

        // Builder pattern
        public static class Builder {
            private final RoleDTO dto = new RoleDTO();

            public Builder id(Long id) {
                dto.id = id;
                return this;
            }

            public Builder roleName(String roleName) {
                dto.roleName = roleName;
                return this;
            }

            public Builder roleCode(String roleCode) {
                dto.roleCode = roleCode;
                return this;
            }

            public Builder description(String description) {
                dto.description = description;
                return this;
            }

            public Builder status(String status) {
                dto.status = status;
                return this;
            }

            public Builder permissions(List<String> permissions) {
                dto.permissions = permissions;
                return this;
            }

            public Builder userCount(Long userCount) {
                dto.userCount = userCount;
                return this;
            }

            public Builder createdAt(Instant createdAt) {
                dto.createdAt = createdAt;
                return this;
            }

            public Builder updatedAt(Instant updatedAt) {
                dto.updatedAt = updatedAt;
                return this;
            }

            public RoleDTO build() {
                return dto;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public Long getId() { return id; }
        public String getRoleName() { return roleName; }
        public String getRoleCode() { return roleCode; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public List<String> getPermissions() { return permissions; }
        public Long getUserCount() { return userCount; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }
    }

    /**
     * 权限选项DTO
     */
    public static class PermissionOptionDTO {
        private String value;
        private String label;
        private String description;
        private String resourceType;

        // Builder pattern
        public static class Builder {
            private final PermissionOptionDTO dto = new PermissionOptionDTO();

            public Builder value(String value) {
                dto.value = value;
                return this;
            }

            public Builder label(String label) {
                dto.label = label;
                return this;
            }

            public Builder description(String description) {
                dto.description = description;
                return this;
            }

            public Builder resourceType(String resourceType) {
                dto.resourceType = resourceType;
                return this;
            }

            public PermissionOptionDTO build() {
                return dto;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public String getValue() { return value; }
        public String getLabel() { return label; }
        public String getDescription() { return description; }
        public String getResourceType() { return resourceType; }
    }
}