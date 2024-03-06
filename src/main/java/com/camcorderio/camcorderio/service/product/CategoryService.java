package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.entity.product.Categories;
import com.camcorderio.camcorderio.model.product.CategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    Page<CategoryDto> getAllCategories(Pageable pageable);
    List<CategoryDto> getAllCategoriesFor();
    Categories getCategoryById(Long id);
    void saveCategory(CategoryDto categoryDto);
    void updateCategory(Long id, CategoryDto categoryDto);

    void deleteCategory(Long id);

    List<Categories> getAllCategoriesForProductList();

    List<Categories> findAllByNonDeleted();

    List<CategoryDto> getTop10SellingCategories();
}
