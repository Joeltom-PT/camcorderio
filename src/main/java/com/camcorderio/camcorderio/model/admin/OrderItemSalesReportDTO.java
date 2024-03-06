package com.camcorderio.camcorderio.model.admin;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderItemSalesReportDTO {
    private LocalDate orderDate;
    private Long totalOrderItems;

    public OrderItemSalesReportDTO(LocalDate orderDate, Long totalOrderItems) {
        this.orderDate = orderDate;
        this.totalOrderItems = totalOrderItems;
    }

}
