package com.camcorderio.camcorderio.model.user;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;

import java.time.LocalDate;


@Data
public class CouponDto {
    private long id;
    private String name;
    private Integer amount;
    private Integer minimumOrderAmount;
    private String couponCode;
    private String couponDescription;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private boolean isDeleted;
    private boolean used;
}
