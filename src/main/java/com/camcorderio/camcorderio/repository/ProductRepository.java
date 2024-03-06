package com.camcorderio.camcorderio.repository;


import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.model.product.ProductMiniDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {

    List<Product> findTop8ByStatusTrueAndBrandStatusTrueAndCategoryStatusTrueAndIsDeletedFalseOrderByPublishedAtDesc();

    Page<Product> findByStatusTrueAndCategoryStatusTrueAndBrandStatusTrueAndIsDeletedFalse(Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "WHERE p.status = true " +
            "AND p.category.status = true " +
            "AND p.brand.status = true " +
            "AND p.isDeleted = false " +
            "AND (LOWER(p.name) LIKE %:productName% " +
            "OR LOWER(p.category.categoryName) LIKE %:categoryName% AND p.category.isDeleted = false " +
            "OR LOWER(p.brand.brandName) LIKE %:brandName% AND p.brand.isDeleted = false) " +
            "AND p.isDeleted = false")
    Page<Product> searchProducts(
            @Param("productName") String productName,
            @Param("categoryName") String categoryName,
            @Param("brandName") String brandName,
            Pageable pageable);


    Page<Product> findByCategoryCategoryIdAndBrandBrandIdAndIsDeletedFalse(Long categoryId, Long brandId, Pageable pageable);

    Optional<Object> findByNameIgnoreCaseAndIsDeletedFalse(String productName);

    List<Product> findAllByIsDeletedFalse();

    

    List<Product> findByBrandBrandIdAndIsDeletedFalse(long id);

    List<Product> findByCategoryCategoryIdAndIsDeletedFalse(long id);

    @Query("SELECT new com.camcorderio.camcorderio.model.product.ProductMiniDto(p.productId, p.name) " +
            "FROM Product p " +
            "WHERE p.category.status = true AND p.category.isDeleted = false " +
            "AND p.brand.status = true AND p.brand.isDeleted = false " +
            "AND p.status = true AND p.isDeleted = false")
    List<ProductMiniDto> findProductMiniDtoByConditions();

    Page<Product> findAllByIsDeletedFalseAndOfferDiscountIsNotNull(Pageable pageable);


    @Query("SELECT c, c.categoryName, c.categoryDescription, c.status, SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "JOIN p.category c " +
            "WHERE p.status = true " +
            "AND c.status = true " +
            "AND c.isDeleted = false " +
            "AND p.brand.status = true " +
            "AND p.brand.isDeleted = false " +
            "AND p.isDeleted = false " +
            "GROUP BY c " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTop10SellingCategories();

    @Query("SELECT b, b.brandName, b.brandDescription, b.status, SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "JOIN p.brand b " +
            "WHERE p.status = true " +
            "AND b.status = true " +
            "AND b.isDeleted = false " +
            "AND p.category.status = true " +
            "AND p.category.isDeleted = false " +
            "AND p.isDeleted = false " +
            "GROUP BY b " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTop10SellingBrands();

}
