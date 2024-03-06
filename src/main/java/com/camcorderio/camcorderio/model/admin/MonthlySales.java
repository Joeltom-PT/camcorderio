package com.camcorderio.camcorderio.model.admin;

import lombok.Data;

@Data
public class MonthlySales {
    private String month;
    private Integer totalAmount;
    private Integer noOfProductSold;
    private Integer noOfOrders;
}
