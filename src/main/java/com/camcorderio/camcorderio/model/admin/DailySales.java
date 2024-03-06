package com.camcorderio.camcorderio.model.admin;

import lombok.Data;

@Data
public class DailySales {
    private String day;
    private Integer totalAmount;
    private Integer noOfProductSold;
    private Integer noOfOrders;
}
