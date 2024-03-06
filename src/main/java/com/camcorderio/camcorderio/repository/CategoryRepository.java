package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.product.Categories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Categories, Long> {

   boolean existsByCategoryNameIgnoreCaseAndIsDeletedFalse(String categoryName);

   List<Categories> findByStatusTrueAndIsDeletedFalse();

    Page<Categories> findAllByIsDeletedFalse(Pageable pageable);


    List<Categories> findByIsDeletedFalseAndStatusTrue();
}
