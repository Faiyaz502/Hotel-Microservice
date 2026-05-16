package com.example.gateway.AdminAPIController;

import com.example.gateway.filter.AuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/admin/blacklist")
public class BlacklistAdminController {

    private final ReactiveStringRedisTemplate redisTemplate;
    private static final String BL_PREFIX = "BLACKLIST:";
    private static final Logger log = LoggerFactory.getLogger(BlacklistAdminController.class);

    public BlacklistAdminController(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ---------- Manually ban an IP ----------

    @PostMapping("/{ip}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<String> banIp(@PathVariable String ip,
                              @RequestParam(defaultValue = "60") int minutes) {

        log.info("Calling the Manual Ban IP Controller ->{}",ip);
        return ReactiveSecurityContextHolder.getContext()
                .map(sc -> sc.getAuthentication().getName())
                .flatMap(adminName -> {
                    String reason = "Banned by admin: " + adminName;
                    return redisTemplate.opsForValue()
                            .set(BL_PREFIX + ip, reason, Duration.ofMinutes(minutes))
                            .thenReturn(String.format("IP %s banned for %d mins by %s", ip, minutes, adminName));
                });
    }

    // ---------- Manually unban an IP ----------

    @DeleteMapping("/{ip}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<String> unbanIp(@PathVariable String ip) {
        log.info("Calling the Manual UnBan IP Controller ->{}",ip);

        return redisTemplate.delete(BL_PREFIX + ip)
                .flatMap(deletedCount -> {
                    if (deletedCount > 0) {
                        return Mono.just("IP " + ip + " has been successfully unbanned.");
                    }
                    return Mono.error(new RuntimeException("IP not found in blacklist."));
                });
    }

    // ---------- List all blacklisted IPs ----------

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<String> listBlacklist(UserDetails authenticatedPrincipal) {

        log.info("Calling the  listBlacklist IP Controller by ->{}",authenticatedPrincipal.getUsername());

        ScanOptions options = ScanOptions.scanOptions()
                .match(BL_PREFIX + "*")
                .count(100)
                .build();

        return redisTemplate.scan(options)
                .flatMap(key -> redisTemplate.opsForValue().get(key)
                        .map(value -> String.format(
                                "IP: %s | Reason: %s",
                                key.replace(BL_PREFIX, ""),
                                value
                        ))
                );
    }
}