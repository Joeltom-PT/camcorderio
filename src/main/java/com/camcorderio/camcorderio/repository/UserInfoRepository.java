package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.user.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserInfoRepository extends JpaRepository<UserInfo, UUID> {

    UserInfo findByEmail(String email);

    Page<UserInfo> findByRoleNot(String role, Pageable pageable);

    Page<UserInfo> findByRoleNotAndEmailContainingIgnoreCase(
            String role,
            String emailQuery,
            Pageable pageable
    );

    UserInfo findByUserId(UUID userId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByReferralCode(String referral);

    UserInfo findByReferralCode(String referral);
}