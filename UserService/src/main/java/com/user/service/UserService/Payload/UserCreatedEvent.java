package com.user.service.UserService.Payload;

import lombok.Builder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private String eventId;
    private String userId;
    private String email;
    private String username;
    private String action;
    private LocalDateTime timestamp;
    private String message;

    // Optional: Convenience constructor for your current usage
    public UserCreatedEvent(String email, String userId, String username) {
        this.email = email;
        this.userId = userId;
        this.username = username;
        this.action = "created";
        this.timestamp = LocalDateTime.now();
        this.eventId = UUID.randomUUID().toString();
        this.message = "User created successfully";
    }
}
