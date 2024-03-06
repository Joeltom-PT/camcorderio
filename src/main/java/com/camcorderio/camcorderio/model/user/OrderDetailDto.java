package com.camcorderio.camcorderio.model.user;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class OrderDetailDto {
    private long orderId;
    private List<OrderItemDto> orderItems;
    private String address;
    private LocalDate orderDate;
    private double totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String orderStatus;
    private int CouponDiscount;
    private LocalDate deliveredDate;
    private LocalDate returnExpiryDate;
    private List<String> orderTrackingStatuses;
}
