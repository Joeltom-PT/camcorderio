package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.entity.product.BrandOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface BrandOfferService {
    Page<BrandOffer> getAllBrandOffers(Pageable pageable);

    BrandOffer getBrandByBrandId(long brandId);

    boolean addOffer(long brandId, int offerPercentage, LocalDate offerEndDate);

    Optional<BrandOffer> getBrandOfferById(long id);

    boolean updateBrandOffer(long offerId, int offerPercentage, LocalDate offerEndDate);

    boolean deleteBrandOffer(long id);

    BrandOffer findBrandOfferByBrandId(Long brandId);
}
