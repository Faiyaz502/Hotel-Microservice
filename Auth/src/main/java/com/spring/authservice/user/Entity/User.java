package com.spring.authservice.user.Entity;




import com.spring.authservice.user.Enums.AuthProviderType;
import com.spring.authservice.user.Enums.UserRole;
import com.spring.authservice.user.Enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


@SoftDelete
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_phone", columnList = "phone_number"),
        @Index(name = "idx_user_status", columnList = "status"),
        @Index(name = "idx_user_role", columnList = "primary_role")
})
public class User extends BaseEntity implements UserDetails {

    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;


    private String username;

    private String password;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private AuthProviderType providerType;


    private String email;


    private String phoneNumber;


    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private UserRole primaryRole ;







    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (primaryRole != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + primaryRole.name()));
        }
        return authorities;
    }




    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }


    public boolean isCustomer() {
        return primaryRole == UserRole.CUSTOMER;
    }

    public boolean isRestaurantOwner() {
        return primaryRole == UserRole.RESTAURANT_OWNER ;
    }

    public boolean isDeliveryPerson() {
        return primaryRole == UserRole.DELIVERY_PERSON ;
    }

    public boolean isAdmin() {
        return primaryRole == UserRole.ADMIN ;
    }

    @PrePersist
    public void prePersist() {
        // If username is not set, generate from firstName + lastName
        if (this.username == null || this.username.isEmpty()) {
            String baseUsername = (firstName + "." + lastName).toLowerCase().replaceAll("\\s+", "");
            this.username = baseUsername;
        }
    }

}