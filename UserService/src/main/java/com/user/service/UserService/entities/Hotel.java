package com.user.service.UserService.entities;

import com.user.service.UserService.Config.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

        @Id
        private String id ;

        private String name ;

        private String location ;


        private String contact ;

        private String about ;


}
