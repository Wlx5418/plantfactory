package com.qzsf.plantfactoryspring.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 * 用于JWT令牌的生成、解析和验证
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:plant-factory-jwt-secret-key-2024}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24小时
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7天
    private Long refreshExpiration;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从令牌中获取特定声明
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从令牌中获取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查令牌是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 为用户生成访问令牌
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    /**
     * 为用户生成刷新令牌
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    /**
     * 创建令牌
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证令牌
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证令牌（不需要用户详情）
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否为访问令牌
     */
    public Boolean isAccessToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return "access".equals(claims.get("type"));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 检查是否为刷新令牌
     */
    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取令牌剩余有效时间（毫秒）
     */
    public Long getRemainingValidity(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (JwtException | IllegalArgumentException e) {
            return 0L;
        }
    }

    /**
     * 获取令牌剩余有效时间（别名方法）
     */
    public Long getTokenRemainingTime(String token) {
        return getRemainingValidity(token);
    }

    /**
     * 刷新访问令牌
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            if (!isRefreshToken(token)) {
                throw new IllegalArgumentException("只能使用刷新令牌来生成新的访问令牌");
            }

            String username = claims.getSubject();
            // 从刷新令牌中提取用户权限信息
            @SuppressWarnings("unchecked")
            java.util.List<String> authorities = (java.util.List<String>) claims.get("authorities");

            // 创建新的访问令牌
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(username)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .claim("type", "access")
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("刷新令牌失败", e);
            return null;
        }
    }
}