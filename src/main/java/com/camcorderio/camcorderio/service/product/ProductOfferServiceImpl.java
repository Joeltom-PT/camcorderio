package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.model.product.ProductOfferDto;
import com.camcorderio.camcorderio.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductOfferServiceImpl implements ProductOfferService {

    private final ProductRepository productRepository;

    public ProductOfferServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    public Page<ProductOfferDto> getAllProductOffers(Pageable pageable) {
        Page<ProductOfferDto> productOfferDtos =  productRepository.findAllByIsDeletedFalseAndOfferDiscountIsNotNull(pageable)
                .map(this::convertToProductOfferDto);
        return productOfferDtos;
    }


    public boolean addOffer(long productId, double offerAmount, LocalDate offerEndDate) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            if (offerAmount > 0 && offerEndDate != null && offerEndDate.isAfter(LocalDate.now())) {
                product.setOfferDiscount(offerAmount);
                product.setOfferExpireDateTime(offerEndDate.atStartOfDay());
                productRepository.save(product);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    @Override
    public boolean updateOffer(long id, double offerAmount, LocalDate offerEndDate) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            if (offerAmount > 0 && offerEndDate != null && offerEndDate.isAfter(LocalDate.now())) {
                product.setOfferDiscount(offerAmount);
                product.setOfferExpireDateTime(offerEndDate.atStartOfDay());
                productRepository.save(product);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteOffer(long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            try {
                product.setOfferDiscount(null);
                product.setOfferExpireDateTime(null);
                product.setOfferCreatedAt(null);
                productRepository.save(product);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }


    private ProductOfferDto convertToProductOfferDto(Product product) {
        ProductOfferDto dto = new ProductOfferDto();
        dto.setId(product.getProductId());
        if (!product.getImagesPath().isEmpty()) {
            dto.setImagePath(product.getImagesPath().get(0));
        }
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setOfferDiscount(product.getOfferDiscount());
        dto.setOfferCreatedAt(product.getOfferCreatedAt());
        dto.setOfferExpireDateTime(product.getOfferExpireDateTime());
        return dto;
    }
}
