package com.qzsf.plantfactoryspring.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT工具类
 * 提供JWT令牌的生成、解析、验证等功能
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        // 确保密钥至少512位（64字节）以满足HS512算法要求
        String base64Secret = jwtSecret;
        if (base64Secret.length() < 64) {
            // 如果密钥太短，使用哈希扩展到64字节
            base64Secret = String.format("%-64s", base64Secret).replace(' ', '0');
        }
        return Keys.hmacShaKeyFor(base64Secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成访问令牌
     *
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateAccessToken(String username) {
        return generateToken(username, jwtExpiration);
    }

    /**
     * 生成刷新令牌
     *
     * @param username 用户名
     * @return 刷新令牌
     */
    public String generateRefreshToken(String username) {
        return generateToken(username, refreshExpiration);
    }

    /**
     * 生成JWT令牌
     *
     * @param username 用户名
     * @param expiration 过期时间（毫秒）
     * @return JWT令牌
     */
    private String generateToken(String username, Long expiration) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(expiration, ChronoUnit.MILLIS);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("从令牌中获取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取令牌的过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("获取令牌过期时间失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证令牌是否有效
     *
     * @param token JWT令牌
     * @param username 用户名
     * @return 是否有效
     */
    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            return tokenUsername != null && tokenUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证令牌是否有效
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查令牌是否过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            log.error("检查令牌过期状态失败: {}", e.getMessage());
            return true;
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
            if (!validateToken(refreshToken)) {
                throw new IllegalArgumentException("刷新令牌无效");
            }

            String username = getUsernameFromToken(refreshToken);
            if (username == null) {
                throw new IllegalArgumentException("无法从刷新令牌中获取用户名");
            }

            return generateAccessToken(username);
        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            throw new IllegalArgumentException("刷新令牌失败: " + e.getMessage());
        }
    }

    /**
     * 获取令牌剩余有效时间（毫秒）
     *
     * @param token JWT令牌
     * @return 剩余有效时间，如果令牌无效返回-1
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            if (expiration == null) {
                return -1;
            }
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            log.error("获取令牌剩余时间失败: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * 检查令牌是否即将过期（30分钟内）
     *
     * @param token JWT令牌
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token) {
        long remainingTime = getTokenRemainingTime(token);
        return remainingTime > 0 && remainingTime < (30 * 60 * 1000); // 30分钟
    }

    /**
     * 解析令牌获取所有声明
     *
     * @param token JWT令牌
     * @return 声明信息
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析令牌声明失败: {}", e.getMessage());
            return null;
        }
    }
}