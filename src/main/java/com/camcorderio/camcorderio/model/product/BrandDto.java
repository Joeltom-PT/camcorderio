package com.camcorderio.camcorderio.model.product;

import lombok.Data;

@Data
public class BrandDto {
    private Long BrandId;
    private String BrandName;
    private String BrandDescription;
    private int sales;
    private boolean status;
}
