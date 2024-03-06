package com.camcorderio.camcorderio.entity.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private Integer amount;
    private Integer minimumOrderAmount;
    private String couponCode;
    private String couponDescription;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserInfo user;
}
