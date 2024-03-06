package com.camcorderio.camcorderio.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private String productImage;
    private Double productPrice;
    private Double offerAmount;
    private int quantity;

}
