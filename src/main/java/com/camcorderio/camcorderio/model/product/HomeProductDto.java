package com.camcorderio.camcorderio.model.product;

import lombok.Data;

import java.util.List;

@Data
public class HomeProductDto {
    private Long productId;
    private String name;
    private String description;
    private double price;
    private String ImagePath;
//    private List<String> imagesPath;
}
