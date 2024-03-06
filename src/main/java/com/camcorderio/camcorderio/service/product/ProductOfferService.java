package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.model.product.ProductOfferDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductOfferService {

//    List<ProductOfferDto> getAllProductOffers();


    boolean addOffer(long productId, double offerAmount, LocalDate offerEndDate);

    Page<ProductOfferDto> getAllProductOffers(Pageable pageable);

    boolean updateOffer(long id, double offerAmount, LocalDate offerEndDate);


    boolean deleteOffer(long id);
}
