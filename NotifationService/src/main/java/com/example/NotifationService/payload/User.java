package com.example.NotifationService.payload;


import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {


    private String userId;

    private String name ;

    private String email ;

    private String phone ;

    private String about ;


}
