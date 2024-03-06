package com.camcorderio.camcorderio.service.others;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Set;

@Service
public class UserRedirectService {

    public String redirectUserByRole(Principal principal) {
        if (principal != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

            if (roles.contains("ADMIN")) {
                return "redirect:/admin/dashboard";
            } else if (roles.contains("USER")) {
                return "redirect:/";
            }

        }

        return null;
    }
}
