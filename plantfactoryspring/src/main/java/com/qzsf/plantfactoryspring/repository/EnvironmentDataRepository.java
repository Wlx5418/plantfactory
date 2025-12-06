package com.qzsf.plantfactoryspring.repository;

import com.qzsf.plantfactoryspring.entity.EnvironmentData;
import org.springframework.stereotype.Repository;

/**
 * 环境数据访问接口 - 别名接口
 * 为了保持代码兼容性，继承自EnvironmentRepository
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Repository
public interface EnvironmentDataRepository extends EnvironmentRepository {
    // 继承EnvironmentRepository的所有方法
    // 这个接口主要是为了保持代码兼容性
}