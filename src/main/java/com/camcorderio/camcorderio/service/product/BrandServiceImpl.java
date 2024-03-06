package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.entity.product.Brands;
import com.camcorderio.camcorderio.exceptions.DuplicateBrandException;
import com.camcorderio.camcorderio.model.product.BrandDto;
import com.camcorderio.camcorderio.repository.BrandRepository;
import com.camcorderio.camcorderio.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    private final ProductRepository productRepository;

    @Autowired
    public BrandServiceImpl(BrandRepository brandRepository, ProductRepository productRepository) {
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Page<BrandDto> getAllBrands(Pageable pageable) {
        Page<Brands> brandsPage = brandRepository.findAllByIsDeletedFalse(pageable);
        return brandsPage.map(this::convertToDto);
    }

    @Override
    public List<BrandDto> getAllBrandsFor() {
        List<Brands> brands = brandRepository.findAllNotDeleted();
        return brands.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<Brands> getAllBrandsForProductList() {
        List<Brands> brands = brandRepository.findByStatusTrueAndIsDeletedFalse();
        return brands;
    }

    @Override
    public List<Brands> findAllByNonDeleted() {
        return brandRepository.findByIsDeletedFalseAndStatusTrue();
    }

    @Override
    public List<BrandDto> getTop10SellingBrand() {
        List<Object[]> topBrands = productRepository.findTop10SellingBrands();

        return topBrands.stream()
                .map(this::mapToBrandDto)
                .collect(Collectors.toList());
    }

    private BrandDto mapToBrandDto(Object[] result) {
        Brands brand = (Brands) result[0];
        String brandName = (String) result[1];
        String brandDescription = (String) result[2];
        boolean status = (boolean) result[3];
        int sales = ((Number) result[4]).intValue();

        BrandDto brandDto = new BrandDto();
        brandDto.setBrandId(brand.getBrandId());
        brandDto.setBrandName(brandName);
        brandDto.setBrandDescription(brandDescription);
        brandDto.setStatus(status);
        brandDto.setSales(sales);

        return brandDto;
    }


    @Override
    public BrandDto getBrandById(Long id) {
        Brands brand = brandRepository.findById(id).orElse(null);
        return (brand != null) ? convertToDto(brand) : null;
    }

    @Override
    public void saveBrand(BrandDto brandDto) {
        if (brandRepository.existsByBrandNameIgnoreCaseAndIsDeletedFalse(brandDto.getBrandName())) {
            throw new DuplicateBrandException("Brand with the same name already exists.");
        }

        Brands brand = convertToEntity(brandDto);
        brandRepository.save(brand);
    }

    @Override
    public void updateBrand(Long id, BrandDto brandDto) {
        Brands existingBrand = brandRepository.findById(id).orElse(null);

        if (existingBrand != null && !existingBrand.getBrandName().equals(brandDto.getBrandName()) &&
                brandRepository.existsByBrandNameIgnoreCaseAndIsDeletedFalse(brandDto.getBrandName())) {
            throw new DuplicateBrandException("Brand with the same name already exists.");
        }

        if (existingBrand != null) {
            existingBrand.setBrandName(brandDto.getBrandName());
            existingBrand.setBrandDescription(brandDto.getBrandDescription());
            existingBrand.setStatus(brandDto.isStatus());
            brandRepository.save(existingBrand);
        }
    }

    @Override
    public void deleteBrand(Long id) {
        Brands brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with id: " + id));

        brand.setDeleted(true);

        brandRepository.save(brand);
    }

    private BrandDto convertToDto(Brands brand) {
        BrandDto brandDto = new BrandDto();
        brandDto.setBrandId(brand.getBrandId());
        brandDto.setBrandName(brand.getBrandName());
        brandDto.setBrandDescription(brand.getBrandDescription());
        brandDto.setStatus(brand.isStatus());
        return brandDto;
    }

    private Brands convertToEntity(BrandDto brandDto) {
        Brands brand = new Brands();
        brand.setBrandId(brandDto.getBrandId());
        brand.setBrandName(brandDto.getBrandName());
        brand.setBrandDescription(brandDto.getBrandDescription());
        brand.setStatus(brandDto.isStatus());
        return brand;
    }
}