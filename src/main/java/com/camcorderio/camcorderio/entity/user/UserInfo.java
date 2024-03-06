package com.camcorderio.camcorderio.entity.user;

import com.camcorderio.camcorderio.entity.product.ProductReview;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Users")
public class UserInfo {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "isEnabled")
    private boolean status = true;


    private String phoneNumber;

    @CreationTimestamp
    private LocalDateTime createdOn;

    private int reportCount;

    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'USER'")
    private String role = "USER";

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Address> userAddresses = new LinkedList<>();

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Cart cart;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Wallet wallet;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<WishList> wishLists = new LinkedList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ReturnRequest> returnRequests = new ArrayList<>();

    private boolean isDeleted;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserCoupon> userCoupons = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ProductReview> productReviews;

    private String referralCode;

    public void addCoupon(Coupon coupon) {
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUser(this);
        userCoupon.setCoupon(coupon);
        userCoupons.add(userCoupon);
        coupon.setUser(this);
    }


}
