package com.odoo.common.security.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author odoo
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * JWT签名密钥
     */
    @Value("${jwt.secret:odoo-java-secret-key-for-jwt-token-sign}")
    private String secret;

    /**
     * JWT过期时间（秒）
     */
    @Value("${jwt.expiration:7200}")
    private Long expiration;

    /**
     * JWT刷新Token过期时间（秒）
     */
    @Value("${jwt.refresh-expiration:604800}")
    private Long refreshExpiration;

    /**
     * JWT发行者
     */
    @Value("${jwt.issuer:odoo-java}")
    private String issuer;

    /**
     * 获取签名密钥
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成访问Token
     */
    public String generateAccessToken(String subject, Map<String, Object> claims) {
        return generateToken(subject, claims, expiration);
    }

    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(String subject) {
        return generateToken(subject, null, refreshExpiration);
    }

    /**
     * 生成Token
     */
    public String generateToken(String subject, Map<String, Object> claims, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignKey(), SignatureAlgorithm.HS512);

        if (claims != null && !claims.isEmpty()) {
            builder.setClaims(claims);
            // subject可能被claims覆盖，重新设置
            builder.setSubject(subject);
        }

        return builder.compact();
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.warn("从Token获取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取Claims
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从Token中获取特定的Claim
     */
    public Object getClaimFromToken(String token, String claimName) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get(claimName);
        } catch (Exception e) {
            log.warn("从Token获取Claim失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取Token过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.warn("从Token获取过期时间失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            log.warn("检查Token过期状态失败: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        if (StrUtil.isBlank(token)) {
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.warn("Token签名无效: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token格式不正确: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的Token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token参数为空: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 验证Token并返回用户名
     */
    public String validateTokenAndGetUsername(String token) {
        if (validateToken(token)) {
            return getUsernameFromToken(token);
        }
        return null;
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String subject = claims.getSubject();

            // 移除时间相关的claims
            claims.remove(Claims.ISSUED_AT);
            claims.remove(Claims.EXPIRATION);
            claims.remove(Claims.NOT_BEFORE);

            return generateAccessToken(subject, claims);
        } catch (Exception e) {
            log.warn("刷新Token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取Token剩余有效时间（秒）
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            if (expiration == null) {
                return 0;
            }
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining / 1000);
        } catch (Exception e) {
            log.warn("获取Token剩余时间失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 创建用于登录的Token Claims
     */
    public static Map<String, Object> createLoginClaims(Long userId, String username,
                                                        String realName, String roleCode) {
        return Map.of(
                "userId", userId,
                "username", username,
                "realName", realName != null ? realName : "",
                "roleCode", roleCode != null ? roleCode : "",
                "type", "access"
        );
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Object userId = getClaimFromToken(token, "userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }

    /**
     * 从Token中获取用户真实姓名
     */
    public String getRealNameFromToken(String token) {
        Object realName = getClaimFromToken(token, "realName");
        return realName != null ? realName.toString() : null;
    }

    /**
     * 从Token中获取角色代码
     */
    public String getRoleCodeFromToken(String token) {
        Object roleCode = getClaimFromToken(token, "roleCode");
        return roleCode != null ? roleCode.toString() : null;
    }
} 