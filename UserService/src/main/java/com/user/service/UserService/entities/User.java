package com.user.service.UserService.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "main_users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

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
