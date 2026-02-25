package com.user.service.UserService.Config;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Common fields for all entities
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
