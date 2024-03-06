package com.camcorderio.camcorderio.model.user;

import lombok.Data;

@Data
public class OrderItemDto {
    private String productName;
    private Integer quantity;
    private String imageUrl;
}
