package com.camcorderio.camcorderio.model.admin;

import lombok.Data;

@Data
public class WeeklySales {
    private String weeks;
    private Integer totalAmount;
    private Integer noOfProductSold;
    private Integer noOfOrders;
}
