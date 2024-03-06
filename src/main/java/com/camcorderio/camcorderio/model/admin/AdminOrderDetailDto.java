package com.camcorderio.camcorderio.model.admin;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AdminOrderDetailDto {
    private Long orderId;
    private UUID userId;
    private String username;
    private String email;
    private String status;
    private String userProfileImage;
    private String phoneNumber;
    private AdminAddressDto address;
    private LocalDate orderDate;
    private double totalAmount;
    private String paymentStatus;
    private String deliveryStatus;
    private LocalDate deliveredDate;
    private LocalDate returnExpiryDate;
    private boolean cancelled;
    private boolean refundStatus;
}
