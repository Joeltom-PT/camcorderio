package com.camcorderio.camcorderio.controller;

import com.camcorderio.camcorderio.exceptions.DuplicateBrandException;
import com.camcorderio.camcorderio.model.product.BrandDto;
import com.camcorderio.camcorderio.model.product.CategoryDto;
import com.camcorderio.camcorderio.service.product.BrandService;
import com.camcorderio.camcorderio.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/brands")
public class BrandController {

    private final BrandService brandService;

    private final ProductService productService;

    @Autowired
    public BrandController(BrandService brandService, ProductService productService) {
        this.brandService = brandService;
        this.productService = productService;
    }

    @GetMapping
    public String getAllBrands(Model model, @PageableDefault(size = 10) Pageable pageable) {
        Page<BrandDto> brandsPage = brandService.getAllBrands(pageable);
        model.addAttribute("brand", new BrandDto());
        model.addAttribute("brands", brandsPage);
        return "admin/brand/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("brand", new BrandDto());
        return "admin/brand/add";
    }

    @PostMapping("/add")
    public String addBrand(@ModelAttribute("brand") BrandDto brandDto, RedirectAttributes attributes) {
        try {
            brandService.saveBrand(brandDto);
            return "redirect:/admin/brands";
        } catch (DuplicateBrandException e) {
            attributes.addAttribute("error", "Error adding brand: " + e.getMessage());
            return "redirect:/admin/brands";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        BrandDto brandDto = brandService.getBrandById(id);
        model.addAttribute("brand", brandDto);
        return "admin/brand/edit";
    }

    @PostMapping("/edit/{id}")
    public String editBrand(@PathVariable("id") Long id, @ModelAttribute("brand") BrandDto brandDto, Model model) {
        try {
            brandService.updateBrand(id, brandDto);
            return "redirect:/admin/brands";
        } catch (DuplicateBrandException e) {
            model.addAttribute("error", "Error editing brand: " + e.getMessage());
            return "admin/brand/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteBrand(@PathVariable("id") Long id, RedirectAttributes attributes) {
        boolean exist = productService.brandExist(id);
        if (exist) {
            attributes.addFlashAttribute("error", "Products exist for this brand. Deletion not allowed.");
        } else {
            brandService.deleteBrand(id);
            attributes.addFlashAttribute("success", "Deletion successfully completed");
        }
        return "redirect:/admin/brands";
    }
}