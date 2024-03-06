package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.entity.product.Categories;
import com.camcorderio.camcorderio.exceptions.DuplicateCategoryException;
import com.camcorderio.camcorderio.model.product.CategoryDto;
import com.camcorderio.camcorderio.repository.CategoryRepository;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public Page<CategoryDto> getAllCategories(Pageable pageable) {
        Page<Categories> categories = categoryRepository.findAllByIsDeletedFalse(pageable);
        return categories.map(this::convertToDto);
    }

    @Override
    public List<CategoryDto> getAllCategoriesFor() {
        List<Categories> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Categories> getAllCategoriesForProductList() {
        List<Categories> categories = categoryRepository.findByStatusTrueAndIsDeletedFalse();

        return categories;
    }

    @Override
    public List<Categories> findAllByNonDeleted() {
        return categoryRepository.findByIsDeletedFalseAndStatusTrue();
    }

    @Override
    public List<CategoryDto> getTop10SellingCategories() {
        List<Object[]> topCategories = productRepository.findTop10SellingCategories();

        return topCategories.stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }
    private CategoryDto mapToCategoryDto(Object[] result) {
        Categories category = (Categories) result[0];
        String categoryName = (String) result[1];
        String categoryDescription = (String) result[2];
        boolean status = (boolean) result[3];
        int sales = ((Number) result[4]).intValue();

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setCategoryId(category.getCategoryId());
        categoryDto.setCategoryName(categoryName);
        categoryDto.setCategoryDescription(categoryDescription);
        categoryDto.setStatus(status);
        categoryDto.setSales(sales);

        return categoryDto;
    }



    public Categories getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public void saveCategory(CategoryDto categoryDto) {
        if (categoryRepository.existsByCategoryNameIgnoreCaseAndIsDeletedFalse(categoryDto.getCategoryName())) {
            throw new DuplicateCategoryException("Category with the same name already exists.");
        }

        Categories category = convertToEntity(categoryDto);
        categoryRepository.save(category);
    }

    public void updateCategory(Long id, CategoryDto categoryDto) {
        Categories existingCategory = categoryRepository.findById(id).orElse(null);

        if (existingCategory != null && !existingCategory.getCategoryName().equals(categoryDto.getCategoryName()) &&
                categoryRepository.existsByCategoryNameIgnoreCaseAndIsDeletedFalse(categoryDto.getCategoryName())) {
            throw new DuplicateCategoryException("Category with the same name already exists.");
        }

        if (existingCategory != null) {
            existingCategory.setCategoryName(categoryDto.getCategoryName());
            existingCategory.setCategoryDescription(categoryDto.getCategoryDescription());
            existingCategory.setStatus(categoryDto.isStatus());
            categoryRepository.save(existingCategory);
        }
    }

    @Override
    public void deleteCategory(Long id) {
        Categories category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        category.setDeleted(true);

        categoryRepository.save(category);
    }

    private Categories convertToEntity(CategoryDto categoryDto) {
        Categories category = new Categories();
        category.setCategoryId(categoryDto.getCategoryId());
        category.setCategoryName(categoryDto.getCategoryName());
        category.setCategoryDescription(categoryDto.getCategoryDescription());
        category.setStatus(categoryDto.isStatus());
        return category;
    }

    private CategoryDto convertToDto(Categories category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setCategoryId(category.getCategoryId());
        categoryDto.setCategoryName(category.getCategoryName());
        categoryDto.setCategoryDescription(category.getCategoryDescription());
        categoryDto.setStatus(category.isStatus());
        return categoryDto;
    }
}