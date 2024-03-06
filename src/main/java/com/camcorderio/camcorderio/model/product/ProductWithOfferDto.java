package com.camcorderio.camcorderio.model.product;

import com.camcorderio.camcorderio.entity.product.Brands;
import com.camcorderio.camcorderio.entity.product.Categories;
import com.camcorderio.camcorderio.entity.product.ProductReview;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductWithOfferDto {
    private Long productId;
    private Double price;
    private String name;
    private String description;
    private String specifications;
    private Integer stock;
    private List<String> imagesPath;
    private Brands brands;
    private Categories categories;
    private ProductReview productReview;
    private double offerAmount;
}
