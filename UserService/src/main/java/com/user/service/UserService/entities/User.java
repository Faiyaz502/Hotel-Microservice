package com.user.service.UserService.entities;

import com.user.service.UserService.Config.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "main_users",indexes = {

        @Index(name = "idx_user_id",columnList = "userId"),
        @Index(name = "idx_user_name",columnList = "name"),
        @Index(name = "idx_user_email",columnList = "email"),
        @Index(name = "idx_user_phone",columnList = "phone"),

})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @Column(name = "ID")
    private String userId;

    @Column(length = 20)
    private String name ;

    private String email ;

    private String phone ;

    private String about ;

    @Transient
    private List<Ratings> ratings = new ArrayList<>();



}
