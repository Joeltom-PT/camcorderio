package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.user.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserCouponRepository extends JpaRepository<UserCoupon,Long> {
    boolean existsByUserUserIdAndCouponId(UUID userId, long couponId);
}
