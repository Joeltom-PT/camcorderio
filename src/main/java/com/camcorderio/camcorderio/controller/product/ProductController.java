package com.camcorderio.camcorderio.controller.product;

import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.model.product.ProductDto;
import com.camcorderio.camcorderio.repository.BrandRepository;
import com.camcorderio.camcorderio.repository.CategoryRepository;
import com.camcorderio.camcorderio.entity.product.Brands;
import com.camcorderio.camcorderio.entity.product.Categories;
import com.camcorderio.camcorderio.service.product.BrandService;
import com.camcorderio.camcorderio.service.product.CartService;
import com.camcorderio.camcorderio.service.product.CategoryService;
import com.camcorderio.camcorderio.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductService productService;
    private final BrandService brandService;

    private final CategoryService categoryService;

    @Autowired
    public ProductController(ProductService productService, BrandService brandService, CategoryService categoryService) {
        this.productService = productService;
        this.brandService = brandService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String productList(Model model){
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/product/list";
    }

    @GetMapping("/add")
    public String showAddProductPage(Model model) {
        model.addAttribute("productDto", new ProductDto());

        List<Brands> brandsList = brandService.findAllByNonDeleted();
        List<Categories> categoriesList = categoryService.findAllByNonDeleted();

        model.addAttribute("brands", brandsList);
        model.addAttribute("categories", categoriesList);

        return "admin/product/add";
    }



    @PostMapping("/add")
    public String addProduct(@ModelAttribute @Validated ProductDto productDto,
                             @RequestParam(value = "imageUrls", required = false) List<String> images, Model model) {


        if (productService.isProductNameExists(productDto.getName()) ||
                images == null||
                productDto.getPrice() < 1 ||
                productDto.getStock() < 0) {


            model.addAttribute("productDto", new ProductDto());

            List<Brands> brandsList = brandService.findAllByNonDeleted();
            List<Categories> categoriesList = categoryService.findAllByNonDeleted();

            if (productService.isProductNameExists(productDto.getName())) {
                model.addAttribute("nameError", "Product name already exists. Please choose a different name.");
            }
            if (images == null){
                model.addAttribute("imageError","Image is required");
            }
            if (productDto.getPrice() < 1) {
                model.addAttribute("priceError", "Invalid price. Price should be more than 0 is needed.");
            }
            if (productDto.getStock() < 0) {
                model.addAttribute("stockError", "Invalid stock. Stock should not allow negative values.");
            }

            model.addAttribute("brands", brandsList);
            model.addAttribute("categories", categoriesList);
            return "admin/product/add";
        }

        productService.addProduct(productDto, images);
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{productId}")
    public String showEditProductPage(@PathVariable Long productId, Model model) {
        Product product = productService.getProductById(productId);
        ProductDto productDto = convertProductToDto(product);

        List<Brands> brandsList = brandService.findAllByNonDeleted();
        List<Categories> categoriesList = categoryService.findAllByNonDeleted();

        model.addAttribute("productDto", productDto);
        model.addAttribute("brands", brandsList);
        model.addAttribute("categories", categoriesList);

        return "admin/product/edit";
    }

    @PostMapping("/edit")
    public String editProduct(@ModelAttribute @Validated ProductDto productDto,
                              @RequestParam("images") List<MultipartFile> images) {
        productService.editProduct(productDto, images);
        return "redirect:/admin/products";
    }

    @GetMapping("/delete/{productId}")
    public String deleteProduct(@PathVariable Long productId) {
        productService.deleteProductById(productId);
        return "redirect:/admin/products";
    }

    private ProductDto convertProductToDto(Product product) {
        ProductDto productDto = new ProductDto();

        productDto.setProductId(product.getProductId());
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setSpecifications(product.getSpecifications());
        productDto.setPrice(product.getPrice());
        productDto.setStock(product.getStock());
        productDto.setStatus(product.isStatus());
        productDto.setImagesPath(product.getImagesPath());

        if (product.getCategory() != null) {
            productDto.setCategoryId(product.getCategory().getCategoryId());
        }

        if (product.getBrand() != null) {
            productDto.setBrandId(product.getBrand().getBrandId());
        }

        return productDto;
    }

    private boolean areImageTypesAllowed(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return false;
        }

        List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/jpga", "image/webp");

        for (MultipartFile image : images) {
            if (!allowedTypes.contains(image.getContentType())) {
                return false;
            }
        }

        return true;
    }

}