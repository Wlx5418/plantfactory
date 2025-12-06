package com.qzsf.plantfactoryspring.service;

import com.qzsf.plantfactoryspring.dto.UserDTO;
import com.qzsf.plantfactoryspring.dto.auth.LoginRequest;
import com.qzsf.plantfactoryspring.dto.auth.LoginResponse;
import com.qzsf.plantfactoryspring.entity.User;
import com.qzsf.plantfactoryspring.repository.UserRepository;
import com.qzsf.plantfactoryspring.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 简化版认证服务 - 先修复登录问题
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * 用户登录 - 简化版本
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        log.debug("用户登录请求: {}", username);

        try {
            // 直接从数据库验证用户
            User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new BadCredentialsException("用户名或密码错误"));

            // 验证密码
            log.debug("输入密码: {}, 数据库密码: {}", password, user.getPassword());

            // 验证密码
            boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
            log.debug("密码匹配结果: {} (输入密码: {}, 数据库密码: {})", passwordMatch, password, user.getPassword());

            if (!passwordMatch) {
                throw new BadCredentialsException("用户名或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                throw new BadCredentialsException("用户账户已被禁用");
            }

            // 生成JWT令牌
            String[] roleArray = user.getRoleCodes().toArray(new String[0]);
            org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(roleArray)
                    .build();

            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);

            // 构建用户信息DTO
            UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .realName(user.getRealName())
                .status(user.getStatus().toString())
                .build();

            // 构建登录响应
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtExpiration / 1000) // 转换为秒
                    .userInfo(userDTO)
                    .loginTime(LocalDateTime.now())
                    .build();

        } catch (AuthenticationException e) {
            log.warn("用户登录失败: {} - {}", username, e.getMessage());
            throw new BadCredentialsException("用户名或密码错误");
        }
    }

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    public String refreshToken(String refreshToken) {
        try {
            return jwtUtil.refreshToken(refreshToken);
        } catch (Exception e) {
            log.warn("刷新令牌失败: {}", e.getMessage());
            throw new BadCredentialsException("刷新令牌无效");
        }
    }

    /**
     * 验证用户密码
     *
     * @param rawPassword 原始密码
     * @param encodedPassword 加密密码
     * @return 是否匹配
     */
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 编码密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 用户登出
     *
     * @param username 用户名
     */
    @Transactional
    public void logout(String username) {
        log.info("用户登出: {}", username);
        // 这里可以添加登出时的业务逻辑，如清除缓存等
    }

    /**
     * 检查令牌是否有效
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }
}