package com.camcorderio.camcorderio.entity.user;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Orders {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ORDER_SEQ")
    @SequenceGenerator(name="ORDER_SEQ", sequenceName="ORDER_SEQ", allocationSize=999)
    private Long orderId;


    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL)
    private List<OrderItem> orderProducts;


    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(name = "orderDate")
    private LocalDate orderDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo user;


    @Column(name = "amount")
    private double totalAmount;

    @OneToOne(mappedBy = "orders", cascade = {CascadeType.ALL},orphanRemoval = true)
    private Payments payments;

    @Column(name = "status")
    private String status = "Processing";

    @Column(name = "amountStatus")
    private String amountStatus;

    @Column(name = "deliveryDate")
    private LocalDate deliveredDate;

    @Column(name = "returnExpiryDate")
    private LocalDate returnExpiryDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<ReturnRequest> returnRequests = new ArrayList<>();

    @Column(name = "cancelled")
    private boolean cancelled = false;

    @Column(name = "refundStatus")
    private boolean refundStatus = false;

    private double couponDiscount;

}
