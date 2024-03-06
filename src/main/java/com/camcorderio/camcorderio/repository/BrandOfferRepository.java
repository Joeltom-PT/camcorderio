package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.product.BrandOffer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandOfferRepository extends JpaRepository<BrandOffer,Long> {

    BrandOffer findByBrandsBrandId(long brandId);
}
