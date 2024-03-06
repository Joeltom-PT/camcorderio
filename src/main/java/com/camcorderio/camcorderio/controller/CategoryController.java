package com.camcorderio.camcorderio.controller;

import com.camcorderio.camcorderio.entity.product.Categories;
import com.camcorderio.camcorderio.exceptions.DuplicateCategoryException;
import com.camcorderio.camcorderio.model.product.CategoryDto;
import com.camcorderio.camcorderio.service.product.CategoryService;
import com.camcorderio.camcorderio.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    private final ProductService productService;

    @Autowired
    public CategoryController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping
    public String listCategories(Model model, @PageableDefault(size = 10)Pageable pageable) {
        Page<CategoryDto> categories = categoryService.getAllCategories(pageable);
        model.addAttribute("category", new CategoryDto());
        model.addAttribute("categories", categories);
        return "admin/category/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new CategoryDto());
        return "admin/category/add";
    }

    @PostMapping("/add")
    public String addCategory(@ModelAttribute("category") CategoryDto categoryDto,Model model,RedirectAttributes attributes) {
        try {
            categoryService.saveCategory(categoryDto);
            attributes.addFlashAttribute("success","Category created successfully");
            return "redirect:/admin/categories";
        } catch (DuplicateCategoryException e) {
            attributes.addAttribute("error", "Error adding brand: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Categories category = categoryService.getCategoryById(id);

        CategoryDto categoryDto = convertToDto(category);

        model.addAttribute("category", categoryDto);
        return "admin/category/edit";
    }

    @PostMapping("/edit/{id}")
    public String editCategory(@PathVariable("id") Long id, @ModelAttribute("category") CategoryDto categoryDto,Model model,RedirectAttributes attributes) {
        try {
            categoryService.updateCategory(id, categoryDto);
            attributes.addFlashAttribute("success","Category edit successfully");
            return "redirect:/admin/categories";
        } catch (DuplicateCategoryException e) {
            model.addAttribute("error", "Error adding brand: " + e.getMessage());
            return "redirect:/admin/categories/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes attributes) {
        boolean exist = productService.categoryExist(id);
        if (exist) {
            attributes.addFlashAttribute("error", "Products exist for this category. Deletion not allowed.");
        } else {
            categoryService.deleteCategory(id);
            attributes.addFlashAttribute("success", "Deletion successfully completed");
        }
        return "redirect:/admin/categories";
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
