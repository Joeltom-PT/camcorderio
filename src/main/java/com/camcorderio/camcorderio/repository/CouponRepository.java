package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.user.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon,Long> {
    boolean existsByName(String couponName);

    boolean existsByNameAndIdNot(String couponName, long excludeId);

    Coupon findByCouponCode(String couponCode);

    List<Coupon> findAllByExpiryDateAfter(LocalDate currentDate);

    List<Coupon> findAllByIsDeletedFalse();


    List<Coupon> findAllByExpiryDateAfterAndIsDeletedFalse(LocalDate expiryDate);

}
