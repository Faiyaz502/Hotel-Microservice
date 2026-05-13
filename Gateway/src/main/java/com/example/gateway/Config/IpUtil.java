package com.example.gateway.Config;

import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

public class IpUtil {

    public static String getClientIp(ServerWebExchange exchange) {

        String xForwardedFor = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");
    }
}
