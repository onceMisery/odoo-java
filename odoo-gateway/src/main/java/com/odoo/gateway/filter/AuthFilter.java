package com.odoo.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.odoo.common.core.constants.CommonConstants;
import com.odoo.common.core.result.Result;
import com.odoo.common.core.result.ResultCode;
import com.odoo.common.security.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT认证过滤器
 *
 * @author odoo
 */
@Slf4j
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${gateway.exclude-auth-paths:}")
    private List<String> excludeAuthPaths;

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // 检查是否需要跳过认证
            if (shouldSkipAuth(path)) {
                return chain.filter(exchange);
            }

            // 获取Token
            String token = getTokenFromRequest(request);
            
            if (StrUtil.isBlank(token)) {
                return unauthorizedResponse(exchange, "缺少认证Token");
            }

            // 验证Token
            if (!jwtUtils.validateToken(token)) {
                return unauthorizedResponse(exchange, "Token无效或已过期");
            }

            // 从Token中获取用户信息
            String username = jwtUtils.getUsernameFromToken(token);
            Long userId = jwtUtils.getUserIdFromToken(token);
            
            if (StrUtil.isBlank(username) || userId == null) {
                return unauthorizedResponse(exchange, "Token中用户信息无效");
            }

            // 将用户信息添加到请求头中传递给下游服务
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId.toString())
                    .header("X-Username", username)
                    .header("X-Real-Name", jwtUtils.getRealNameFromToken(token))
                    .header("X-Role-Code", jwtUtils.getRoleCodeFromToken(token))
                    .build();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            return chain.filter(modifiedExchange);
        };
    }

    /**
     * 检查是否应该跳过认证
     */
    private boolean shouldSkipAuth(String path) {
        if (excludeAuthPaths == null || excludeAuthPaths.isEmpty()) {
            return false;
        }

        return excludeAuthPaths.stream().anyMatch(excludePath -> {
            if (excludePath.endsWith("/**")) {
                String prefix = excludePath.substring(0, excludePath.length() - 3);
                return path.startsWith(prefix);
            } else if (excludePath.endsWith("/*")) {
                String prefix = excludePath.substring(0, excludePath.length() - 2);
                return path.startsWith(prefix) && path.indexOf('/', prefix.length()) == -1;
            } else {
                return path.equals(excludePath);
            }
        });
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(ServerHttpRequest request) {
        // 优先从Header中获取Authorization
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(CommonConstants.Http.TOKEN_PREFIX)) {
            return authHeader.substring(CommonConstants.Http.TOKEN_PREFIX.length());
        }

        // 其次从Header中获取X-Token
        String tokenHeader = request.getHeaders().getFirst(CommonConstants.Http.HEADER_TOKEN);
        if (StrUtil.isNotBlank(tokenHeader)) {
            return tokenHeader;
        }

        // 最后从Query参数中获取token
        return request.getQueryParams().getFirst("token");
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        try {
            Result<Void> result = Result.error(ResultCode.UNAUTHORIZED, message);
            String body = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("序列化响应失败", e);
            return response.setComplete();
        }
    }

    /**
     * 配置类
     */
    public static class Config {
        // 可以添加配置参数
    }
} 