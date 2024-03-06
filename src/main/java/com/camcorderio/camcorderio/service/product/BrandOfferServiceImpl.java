package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.entity.product.BrandOffer;
import com.camcorderio.camcorderio.entity.product.Brands;
import com.camcorderio.camcorderio.repository.BrandOfferRepository;
import com.camcorderio.camcorderio.repository.BrandRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
public class BrandOfferServiceImpl implements BrandOfferService{

    private final BrandOfferRepository brandOfferRepository;

    private final BrandRepository brandsRepository;

    public BrandOfferServiceImpl(BrandOfferRepository brandOfferRepository, BrandRepository brandsRepository) {
        this.brandOfferRepository = brandOfferRepository;
        this.brandsRepository = brandsRepository;
    }


    @Override
    public Page<BrandOffer> getAllBrandOffers(Pageable pageable) {
        return brandOfferRepository.findAll(pageable);
    }


    @Override
    public BrandOffer getBrandByBrandId(long brandId) {
        return brandOfferRepository.findByBrandsBrandId(brandId);
    }

    @Override
    public boolean addOffer(long brandId, int offerPercentage, LocalDate offerEndDate) {

        Brands brand = brandsRepository.findById(brandId).orElse(null);

        if (brand == null) {

            return false;
        }

        BrandOffer brandOffer = new BrandOffer();
        brandOffer.setDiscount(offerPercentage);
        brandOffer.setExpireDateTime(offerEndDate.atStartOfDay());
        brandOffer.setBrands(brand);

        brandOfferRepository.save(brandOffer);

        return true;
    }

    @Override
    public Optional<BrandOffer> getBrandOfferById(long id) {
        return brandOfferRepository.findById(id);
    }

    @Override
    public boolean updateBrandOffer(long offerId, int offerPercentage, LocalDate offerEndDate) {
        Optional<BrandOffer> optionalBrandOffer = brandOfferRepository.findById(offerId);

        if (optionalBrandOffer.isPresent()) {
            BrandOffer brandOffer = optionalBrandOffer.get();
            brandOffer.setDiscount(offerPercentage);
            brandOffer.setExpireDateTime(offerEndDate.atStartOfDay());

            brandOfferRepository.save(brandOffer);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteBrandOffer(long id) {
        Optional<BrandOffer> brandOfferOptional = brandOfferRepository.findById(id);
        if (brandOfferOptional.isPresent()) {
            brandOfferRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public BrandOffer findBrandOfferByBrandId(Long brandId) {
        return brandOfferRepository.findByBrandsBrandId(brandId);
    }


}
