package com.camcorderio.camcorderio.model.user;

import com.camcorderio.camcorderio.entity.user.UserInfo;
import org.springframework.stereotype.Component;

@Component
public class UserInfoMapper {

    public UserInfo userInfoDTOtoUserInfo(UserInfoDTO userInfoDTO) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(userInfoDTO.getUsername());
        userInfo.setEmail(userInfoDTO.getEmail());
        userInfo.setPassword(userInfoDTO.getPassword());
        userInfo.setPhoneNumber(userInfoDTO.getPhoneNumber());
        return userInfo;
    }

    public UserInfoDTO userInfoToUserInfoDTO(UserInfo userInfo) {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setPassword(userInfo.getPassword());
        userInfoDTO.setUsername(userInfo.getUsername());
        userInfoDTO.setEmail(userInfo.getEmail());
        userInfoDTO.setPhoneNumber(userInfo.getPhoneNumber());
        return userInfoDTO;
    }
}
