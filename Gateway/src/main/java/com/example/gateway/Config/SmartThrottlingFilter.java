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
@Order(1)
public class SmartThrottlingFilter implements GlobalFilter {

    private final ReactiveStringRedisTemplate redisTemplate;

    private static final String COUNT_PREFIX = "REQ_COUNT:";
    private static final String BL_PREFIX = "BLACKLIST:";

    // How many 429s within the window before auto-ban
    private static final long BAN_THRESHOLD = 5;

    // Sliding window for counting 429 hits
    private static final Duration COUNT_WINDOW = Duration.ofMinutes(5);

    // How long the auto-ban lasts
    private static final Duration BAN_DURATION = Duration.ofMinutes(15);

    public SmartThrottlingFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return chain.filter(exchange)
                .then(Mono.defer(() -> {

                    if (exchange.getResponse().getStatusCode() != HttpStatus.TOO_MANY_REQUESTS) {
                        return Mono.empty();
                    }

                    String ip = IpUtil.getClientIp(exchange);
                    String countKey = COUNT_PREFIX + ip;
                    String banKey = BL_PREFIX + ip;


                    return redisTemplate.opsForValue()
                            .setIfAbsent(countKey, "0", COUNT_WINDOW)   // only sets TTL on first creation
                            .then(redisTemplate.opsForValue().increment(countKey))
                            .flatMap(count -> {

                                if (count >= BAN_THRESHOLD) {
                                    // Threshold reached — ban the IP and clean up the counter
                                    return redisTemplate.opsForValue()
                                            .set(banKey, "Auto-banned after " + count + " rate-limit violations", BAN_DURATION)
                                            .then(redisTemplate.delete(countKey))
                                            .then();
                                }

                                return Mono.empty();
                            });
                }));
    }
}