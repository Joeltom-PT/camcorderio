package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.entity.product.ProductReview;
import com.camcorderio.camcorderio.entity.user.UserInfo;
import com.camcorderio.camcorderio.repository.ProductRepository;
import com.camcorderio.camcorderio.repository.ProductReviewRepository;
import com.camcorderio.camcorderio.repository.UserInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProductReviewServiceImpl implements ProductReviewService{

    private final ProductReviewRepository productReviewRepository;

    private final UserInfoRepository userInfoRepository;

    private final ProductRepository productRepository;

    public ProductReviewServiceImpl(ProductReviewRepository productReviewRepository, UserInfoRepository userInfoRepository, ProductRepository productRepository) {
        this.productReviewRepository = productReviewRepository;
        this.userInfoRepository = userInfoRepository;
        this.productRepository = productRepository;
    }


    @Override
    public void addProductReview(UUID userId, Long productId, int rating, String comment) {
        UserInfo user = userInfoRepository.findById(userId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);

        Optional<ProductReview> existingReviewOptional = productReviewRepository.findByUserUserIdAndProductProductId(userId, productId);

        if (existingReviewOptional.isPresent()) {
            ProductReview existingReview = existingReviewOptional.get();
            existingReview.setRating(rating);
            existingReview.setReview(comment);
            productReviewRepository.save(existingReview);
        } else {
            if (user != null && product != null) {
                ProductReview newReview = new ProductReview();
                newReview.setUser(user);
                newReview.setProduct(product);
                newReview.setRating(rating);
                newReview.setReview(comment);
                productReviewRepository.save(newReview);
            }
        }
    }


    @Override
    public boolean isAlreadyExist(UUID userId, Long productId) {
        return productReviewRepository.existsByUserUserIdAndProductProductId(userId, productId);
    }

    @Override
    public Optional<ProductReview> findByUserAndProduct(UUID userId, long productId) {
        return productReviewRepository.findByUserUserIdAndProductProductId(userId,productId);
    }

}
