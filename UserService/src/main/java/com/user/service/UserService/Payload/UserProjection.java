package com.user.service.UserService.Payload;

import java.io.Serializable;

public interface UserProjection extends Serializable {

    String getUserId();
    String getName();
    String getPhone();
    String getEmail();
    String getAbout();


}
