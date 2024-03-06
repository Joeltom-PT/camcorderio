package com.camcorderio.camcorderio.service.user;

import com.camcorderio.camcorderio.entity.user.Coupon;
import com.camcorderio.camcorderio.model.product.CategoryDto;
import com.camcorderio.camcorderio.model.user.CouponDto;

import java.util.List;

public interface CouponService {
    Coupon addCoupon(CouponDto couponDto);

    boolean isCouponNameExists(String name);

    boolean isCouponNameExistsNotId(String couponName, long excludeId);

    List<CouponDto> getAllCoupons();

    CouponDto getCouponById(long id);

    Coupon updateCoupon(CouponDto couponDto);


    CouponDto getCouponByCouponCode(String couponCode);

    public boolean isCouponAlreadyUsedByUser(String userEmail, long couponId);


    boolean isCouponExpired(long id);

    boolean isCouponStarted(long id);

    List<CouponDto> getAllCouponsForUser(String userEmail);

    void couponUsed(long id, String userEmail);

    boolean deleteCoupon(long id);

}
