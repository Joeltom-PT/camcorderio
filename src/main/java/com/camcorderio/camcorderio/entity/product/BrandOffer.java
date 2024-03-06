package com.camcorderio.camcorderio.entity.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class BrandOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int discount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expireDateTime;

    @OneToOne
    @JoinColumn(name = "brand_id", referencedColumnName = "brandId", unique = true)
    private Brands brands;
}
