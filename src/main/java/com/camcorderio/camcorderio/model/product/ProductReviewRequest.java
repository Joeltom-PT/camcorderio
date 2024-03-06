package com.camcorderio.camcorderio.model.product;

import lombok.Data;

@Data
public class ProductReviewRequest {
    private long productId;
    private long rating;
    private String comment;

}
