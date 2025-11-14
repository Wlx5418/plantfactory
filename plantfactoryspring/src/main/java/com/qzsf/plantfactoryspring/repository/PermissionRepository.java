package com.qzsf.plantfactoryspring.repository;

import com.qzsf.plantfactoryspring.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 权限数据访问接口
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据权限编码查找权限
     */
    Optional<Permission> findByCode(String code);

    /**
     * 根据权限名称查找权限
     */
    Optional<Permission> findByName(String name);

    /**
     * 根据资源类型查找权限列表
     */
    List<Permission> findByResourceType(Permission.ResourceType resourceType);

    /**
     * 根据父权限ID查找子权限列表
     */
    List<Permission> findByParentIdOrderBySortOrderAsc(Long parentId);

    /**
     * 查找所有菜单权限（按排序顺序）
     */
    @Query("SELECT p FROM Permission p WHERE p.resourceType = 'MENU' ORDER BY p.sortOrder ASC")
    List<Permission> findAllMenuPermissions();

    /**
     * 根据角色ID查找权限列表
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN Role r WHERE r.id = :roleId AND p MEMBER OF r.permissions")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查找权限列表
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN User u JOIN u.roles r WHERE u.id = :userId AND p MEMBER OF r.permissions")
    List<Permission> findByUserId(@Param("userId") Long userId);

    /**
     * 根据权限编码列表查找权限
     */
    @Query("SELECT p FROM Permission p WHERE p.code IN :codes")
    List<Permission> findByCodeIn(@Param("codes") Set<String> codes);

    /**
     * 检查权限编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查权限名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据资源路径和请求方法查找API权限
     */
    Optional<Permission> findByResourcePathAndHttpMethod(String resourcePath, String httpMethod);

    /**
     * 查找所有API权限
     */
    @Query("SELECT p FROM Permission p WHERE p.resourceType = 'API' ORDER BY p.resourcePath, p.httpMethod")
    List<Permission> findAllApiPermissions();
}