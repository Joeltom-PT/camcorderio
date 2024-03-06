package com.camcorderio.camcorderio.model.product;

import jakarta.persistence.Column;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
public class ProductOfferDto {
    private long id;
    private String imagePath;
    private String name;
    private double price;
    private Double offerDiscount;
    private LocalDateTime offerCreatedAt;
    private LocalDateTime offerExpireDateTime;

}
