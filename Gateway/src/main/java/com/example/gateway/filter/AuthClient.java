package com.example.gateway.filter;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthClient {

    private final WebClient webClient;
    private final Logger log = LoggerFactory.getLogger(AuthClient.class);

    public AuthClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://AUTHSERVICE").build();
    }

    @Retry(name = "authServiceRetry", fallbackMethod = "authFallback")
    @CircuitBreaker(name = "authServiceCB", fallbackMethod = "fallback")
    public Mono<Boolean> validateToken(String token) {
        log.info("----ValidToken Method Calling -------");

        return webClient.get()
                .uri("/login/validate?token={token}", token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnNext(b -> log.info("AuthService response is -> {}", b));
    }

    public Mono<Boolean> authFallback(String token, Throwable ex) {
        log.info("validateToken's Fallback is Calling");
        log.info("Auth Service unavailable: {}", ex.getMessage());
        return Mono.just(false);
    }
}