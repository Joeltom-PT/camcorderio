package com.camcorderio.camcorderio.model.product;

import lombok.Data;

@Data
public class CategoryDto {
    private Long categoryId;
    private String categoryName;
    private String categoryDescription;
    private int sales;
    private boolean status;
}
