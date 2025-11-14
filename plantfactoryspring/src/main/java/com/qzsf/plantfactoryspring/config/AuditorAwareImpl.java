package com.qzsf.plantfactoryspring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 审计信息提供者
 * 用于自动填充审计字段（创建人、更新人等）
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
                log.debug("当前无认证用户或为匿名用户");
                return Optional.empty();
            }

            String username = authentication.getName();
            log.debug("当前审计用户: {}", username);
            return Optional.of(username);

        } catch (Exception e) {
            log.error("获取当前审计用户失败", e);
            return Optional.empty();
        }
    }
}