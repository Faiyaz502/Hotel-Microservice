package com.example.gateway.filter;

import com.example.gateway.exception.InvalidTokenException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouteValidator validator;
    private final AuthClient authClient;
    private final ObjectMapper objectMapper;

    public AuthenticationFilter(RouteValidator validator, AuthClient authClient, ObjectMapper objectMapper) {
        super(Config.class);
        this.validator = validator;
        this.authClient = authClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {

            if (validator.isSercured.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsKey("Authorization")) {
                    return Mono.error(new RuntimeException("Missing Authorization header"));
                }

                String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return Mono.error(new RuntimeException("Invalid Authorization header"));
                }

                String token = authHeader.substring(7);

                // 1. Call your Auth Service to validate the signature/expiration
                return authClient.validateToken(token)
                        .flatMap(isValid -> {
                            if (!Boolean.TRUE.equals(isValid)) {
                                return Mono.error(new InvalidTokenException("The Token Is Invalid"));
                            }

                            // 2. Extract Data for Spring Security Context
                            // We use your AuthUtil's logic: roles are in the "roles" claim
                            List<SimpleGrantedAuthority> authorities = extractAuthorities(token);
                            String username = extractUsername(token);

                            Authentication auth = new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    authorities
                            );

                            // 3. Inject into Reactive Security Context
                            // This allows @PreAuthorize("hasRole('ADMIN')") to work in the Gateway Controllers
                            return chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                        });
            }
            return chain.filter(exchange);
        });
    }

    private List<SimpleGrantedAuthority> extractAuthorities(String token) {
        try {
            JsonNode payloadNode = getPayloadNode(token);
            if (payloadNode.has("roles")) {
                // Your AuthUtil already stores them as ROLE_ADMIN, etc.
                List<String> roles = objectMapper.convertValue(
                        payloadNode.get("roles"),
                        new TypeReference<List<String>>() {}
                );

                return roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Failed to extract roles from token: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private String extractUsername(String token) {
        try {
            return getPayloadNode(token).get("sub").asText();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private JsonNode getPayloadNode(String token) throws Exception {
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        return objectMapper.readTree(payload);
    }

    public static class Config {}
}