package com.camcorderio.camcorderio.model.user;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.SessionAttributes;


@Data
@Component
@SessionAttributes("userSignupInfo")
public class UserInfoDTO {

    private String username;
    private String email;
    private String phoneNumber;
    private String password;
}
