package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.product.Brands;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brands, Long> {

    boolean existsByBrandNameIgnoreCaseAndIsDeletedFalse(String brandName);

    List<Brands> findByStatusTrueAndIsDeletedFalse();

    Page<Brands> findAllByIsDeletedFalse(Pageable pageable);


    List<Brands> findByIsDeletedFalseAndStatusTrue();

    @Query("SELECT b FROM Brands b WHERE b.isDeleted = false")
    List<Brands> findAllNotDeleted();

    @Query("SELECT b, b.brandName, SUM(oi.quantity) " +
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
