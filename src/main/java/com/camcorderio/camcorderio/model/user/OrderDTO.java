package com.camcorderio.camcorderio.model.user;

import com.camcorderio.camcorderio.entity.user.OrderItem;
import com.camcorderio.camcorderio.entity.user.Orders;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class OrderDTO {
    private Long orderId;
    private double totalAmount;
    private int numberOfProducts;
    private String status;
    private String paymentMethod;
    private String actionLink;
    private LocalDate orderDate;

    public OrderDTO(Orders order) {
        this.orderId = order.getOrderId();
        this.totalAmount = order.getTotalAmount();
        this.numberOfProducts = order.getOrderProducts().size();
        this.status = order.getStatus();
        this.paymentMethod = order.getPayments() != null ? order.getPayments().getPaymentMethod().toString() : "";
        this.actionLink = "/user/orderDetails/" + order.getOrderId();
        this.orderDate = order.getOrderDate();
    }
}
