package com.camcorderio.camcorderio.model.mapper;

import com.camcorderio.camcorderio.entity.product.BrandOffer;
import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.model.product.ProductWithOfferDto;

public class ProductMapper {
    public static ProductWithOfferDto mapProductToDto(Product product, BrandOffer brandOffer) {
        ProductWithOfferDto dto = new ProductWithOfferDto();
        dto.setProductId(product.getProductId());
        dto.setPrice(product.getPrice());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setSpecifications(product.getSpecifications());
        dto.setStock(product.getStock());
        dto.setImagesPath(product.getImagesPath());
        dto.setBrands(product.getBrand());
        dto.setCategories(product.getCategory());
        dto.setProductReview(product.getReviews() != null && !product.getReviews().isEmpty() ? product.getReviews().get(0) : null);
        if (product.getOfferDiscount() != null) {
            dto.setOfferAmount(product.getOfferDiscount());
        } else if (brandOffer != null) {
            double offerPrice = product.getPrice() * (brandOffer.getDiscount() / 100.0);
            dto.setOfferAmount(offerPrice);
        } else {
            dto.setOfferAmount(0.0);
        }
        return dto;
    }
}
