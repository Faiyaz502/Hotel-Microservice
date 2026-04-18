package com.example.gateway.Config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Order(1) // Runs after the response comes back from the rate limiter
public class SmartThrottlingFilter implements GlobalFilter {

    private final ReactiveStringRedisTemplate redisTemplate;
    private static final String COUNT_PREFIX = "REQ_COUNT:";
    private static final String BL_PREFIX = "BLACKLIST:";

    public SmartThrottlingFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            if (exchange.getResponse().getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

                // Increment abuse counter in Redis
                redisTemplate.opsForValue().increment(COUNT_PREFIX + ip)
                        .flatMap(count -> {
                            if (count >= 5) { // Threshold for DoS suspicion
                                return redisTemplate.opsForValue().set(BL_PREFIX + ip, "true", Duration.ofMinutes(15))
                                        .then(redisTemplate.delete(COUNT_PREFIX + ip));
                            }
                            return Mono.empty();
                        }).subscribe();
            }
        }));
    }
}
