package com.camcorderio.camcorderio.model.product;

import com.camcorderio.camcorderio.entity.product.Brands;
import com.camcorderio.camcorderio.entity.product.Categories;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailsDTO {
    private Long productId;
    private Double price;
    private String name;
    private String description;
    private String specifications;
    private Integer stock;
    private boolean status;
    private List<String> imagesPath;
    private Categories category;
    private Brands brand;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;
    private boolean inWishlist;
}
