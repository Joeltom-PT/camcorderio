package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.entity.product.Brands;
import com.camcorderio.camcorderio.model.product.BrandDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BrandService {
    Page<BrandDto> getAllBrands(Pageable pageable);
    
    List<BrandDto> getAllBrandsFor();

    BrandDto getBrandById(Long id);

    void saveBrand(BrandDto brandDto);

    void updateBrand(Long id, BrandDto brandDto);

    void deleteBrand(Long id);

    List<Brands> getAllBrandsForProductList();

    List<Brands> findAllByNonDeleted();

    List<BrandDto> getTop10SellingBrand();
}
