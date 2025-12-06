package com.qzsf.plantfactoryspring.repository;

import com.qzsf.plantfactoryspring.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户（排除已删除的用户）
     */
    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    /**
     * 根据邮箱查找用户（排除已删除的用户）
     */
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    /**
     * 检查用户名是否存在（排除已删除的用户）
     */
    boolean existsByUsernameAndIsDeletedFalse(String username);

    /**
     * 检查邮箱是否存在（排除已删除的用户）
     */
    boolean existsByEmailAndIsDeletedFalse(String email);

    /**
     * 根据状态查找用户列表
     */
    List<User> findByStatusAndIsDeletedFalse(User.UserStatus status);

    /**
     * 分页查询用户列表（排除已删除的用户）
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = false " +
           "AND (:username IS NULL OR u.username LIKE %:username%) " +
           "AND (:realName IS NULL OR u.realName LIKE %:realName%) " +
           "AND (:status IS NULL OR u.status = :status)")
    Page<User> findUsersWithFilters(@Param("username") String username,
                                   @Param("realName") String realName,
                                   @Param("status") User.UserStatus status,
                                   Pageable pageable);

    /**
     * 更新用户最后登录时间
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginTime = :loginTime WHERE u.id = :userId")
    int updateLastLoginTime(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * 批量更新用户状态
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id IN :userIds")
    int updateStatusByIds(@Param("userIds") List<Long> userIds, @Param("status") User.UserStatus status);

    /**
     * 软删除用户
     */
    @Modifying
    @Query("UPDATE User u SET u.isDeleted = true, u.deletedAt = :deletedTime WHERE u.id = :userId")
    int softDeleteUser(@Param("userId") Long userId, @Param("deletedTime") LocalDateTime deletedTime);

    /**
     * 统计用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isDeleted = false")
    long countActiveUsers();

    /**
     * 根据角色统计用户数量
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.roleCode = :roleCode AND u.isDeleted = false")
    long countUsersByRole(@Param("roleCode") String roleCode);

    /**
     * 查找最近注册的用户
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = false ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(Pageable pageable);

    /**
     * 根据用户名查找用户（排除已删除的用户），同时加载角色信息
     * 用于Spring Security认证，避免懒加载异常
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username AND u.isDeleted = false")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
}