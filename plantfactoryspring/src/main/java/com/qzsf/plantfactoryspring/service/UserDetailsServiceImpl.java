package com.qzsf.plantfactoryspring.service;

import com.qzsf.plantfactoryspring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户详情服务实现
 * 用于Spring Security认证
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 根据用户名加载用户详情
     *
     * @param username 用户名
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("正在加载用户详情: {}", username);

        return userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> {
                    log.warn("用户不存在或已被删除: {}", username);
                    return new UsernameNotFoundException("用户名或密码错误");
                });
    }

    /**
     * 根据用户ID加载用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByUserId(Long userId) throws UsernameNotFoundException {
        log.debug("正在根据用户ID加载用户详情: {}", userId);

        return userRepository.findById(userId)
                .filter(user -> !user.getIsDeleted())
                .orElseThrow(() -> {
                    log.warn("用户不存在或已被删除: {}", userId);
                    return new UsernameNotFoundException("用户不存在");
                });
    }
}