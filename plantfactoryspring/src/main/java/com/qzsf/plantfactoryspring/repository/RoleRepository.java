package com.qzsf.plantfactoryspring.repository;

import com.qzsf.plantfactoryspring.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 角色数据访问接口
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色编码查找角色
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * 根据角色名称查找角色
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * 根据状态查找角色列表
     */
    List<Role> findByStatus(Role.RoleStatus status);

    /**
     * 查找启用的角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.status = 'ACTIVE'")
    List<Role> findActiveRoles();

    /**
     * 检查角色编码是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 检查角色名称是否存在
     */
    boolean existsByRoleName(String roleName);

    /**
     * 根据角色编码列表查找角色
     */
    @Query("SELECT r FROM Role r WHERE r.roleCode IN :roleCodes AND r.status = 'ACTIVE'")
    List<Role> findByRoleCodeInAndStatusActive(@Param("roleCodes") Set<String> roleCodes);

    /**
     * 根据用户ID查找用户角色
     */
    @Query("SELECT r FROM Role r JOIN User u WHERE u.id = :userId AND r MEMBER OF u.roles")
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * 根据角色名称模糊查询角色（分页）
     */
    Page<Role> findByRoleNameContainingIgnoreCase(String roleName, Pageable pageable);

    /**
     * 根据状态查询角色（分页）
     */
    Page<Role> findByStatus(Role.RoleStatus status, Pageable pageable);

    /**
     * 根据角色名称和状态查询角色（分页）
     */
    Page<Role> findByRoleNameContainingIgnoreCaseAndStatus(String roleName, Role.RoleStatus status, Pageable pageable);
}