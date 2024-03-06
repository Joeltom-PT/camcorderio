package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.product.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    boolean existsByUserUserIdAndProductProductId(UUID userId, Long productId);

    Optional<ProductReview> findByUserUserIdAndProductProductId(UUID userId, Long productId);
}
