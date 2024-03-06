package com.camcorderio.camcorderio.model.admin;

import lombok.Data;

@Data
public class YearlySales {
    private String year;
    private Integer totalAmount;
    private Integer noOfProductSold;
    private Integer noOfOrders;
}
