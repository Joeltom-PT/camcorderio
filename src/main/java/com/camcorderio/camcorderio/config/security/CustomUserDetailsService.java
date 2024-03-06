package com.camcorderio.camcorderio.config.security;

import com.camcorderio.camcorderio.entity.user.UserInfo;
import com.camcorderio.camcorderio.exceptions.UserDisabledException;
import com.camcorderio.camcorderio.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException, DisabledException {
        UserInfo user = userInfoRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        if (!user.isStatus()) {
            throw new UserDisabledException("User is disabled: " + email);
        }

        return new CustomUserDetails(user);
    }
}
