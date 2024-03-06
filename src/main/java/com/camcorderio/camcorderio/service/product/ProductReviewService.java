package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.entity.product.ProductReview;

import java.util.Optional;
import java.util.UUID;

public interface ProductReviewService {
    void addProductReview(UUID userId, Long productId, int rating, String comment);

    boolean isAlreadyExist(UUID userId, Long productId);

    Optional<ProductReview> findByUserAndProduct(UUID userId, long productId);
}
