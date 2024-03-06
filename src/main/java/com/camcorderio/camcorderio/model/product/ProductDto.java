package com.camcorderio.camcorderio.model.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private long ProductId;
    private String name;
    private String description;
    private String specifications;
    private Double price;
    private Integer stock;
    private boolean status;
    private Long categoryId;
    private Long brandId;
    private List<MultipartFile> image;
    private List<String> imagesPath;
    private long totalSold;

}


