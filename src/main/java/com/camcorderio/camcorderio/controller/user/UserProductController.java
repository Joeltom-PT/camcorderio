package com.camcorderio.camcorderio.controller.user;

import com.camcorderio.camcorderio.entity.product.Brands;
import com.camcorderio.camcorderio.entity.product.Categories;
import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.entity.product.ProductReview;
import com.camcorderio.camcorderio.entity.user.UserInfo;
import com.camcorderio.camcorderio.model.product.HomeProductDto;
import com.camcorderio.camcorderio.model.product.ProductWithOfferDto;
import com.camcorderio.camcorderio.service.product.BrandService;
import com.camcorderio.camcorderio.service.product.CategoryService;
import com.camcorderio.camcorderio.service.product.ProductReviewService;
import com.camcorderio.camcorderio.service.product.ProductService;
import com.camcorderio.camcorderio.service.user.UserInfoService;
import jakarta.servlet.http.HttpSession;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Controller
public class UserProductController {

    private final ProductService productService;

    private final CategoryService categoryService;

    private final BrandService brandService;

    private final UserInfoService userInfoService;


    private final ProductReviewService productReviewService;

    public UserProductController(ProductService productService, CategoryService categoryService, BrandService brandService, UserInfoService userInfoService, ProductReviewService productReviewService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.userInfoService = userInfoService;
        this.productReviewService = productReviewService;
    }

    @GetMapping("/product/{productId}")
    public String productDetailPage(@PathVariable long productId , Model model, Principal principal){
        ProductWithOfferDto product = productService.getProductByIdForDetailPage(productId);
        if (principal != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

            if (roles.contains("USER")) {
                model.addAttribute("user",userInfoService.currentUserDetail());
            }

            String email = userInfoService.currentUser();
            boolean inWishlist = productService.isProductInWishlist(productId, email);
            model.addAttribute("inWishlist", inWishlist);

        }
        if (principal != null) {
            Optional<ProductReview> productReviewOptional = productReviewService.findByUserAndProduct(userInfoService.currentUserDetail().getUserId(), productId);
            if (productReviewOptional.isPresent()) {
                model.addAttribute("productReview", productReviewOptional.get());
            }
        }


        model.addAttribute("user",userInfoService.currentUserDetail());
        model.addAttribute("product" , product);
        return "user/product-details";
    }

    @GetMapping("/products")
    public String productListPage(@PageableDefault(size = 8) Pageable pageable, Model model, HttpSession session,Principal principal) {
        List<Categories> categories = categoryService.getAllCategoriesForProductList();
        List<Brands> brands = brandService.getAllBrandsForProductList();
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);

        if (session.getAttribute("SearchProducts") != null) {
            Page<HomeProductDto> searchProducts = (Page<HomeProductDto>) session.getAttribute("SearchProducts");
            model.addAttribute("products", searchProducts);
            session.removeAttribute("SearchProducts");
        } else if (session.getAttribute("filteredProducts") != null) {
            Page<HomeProductDto> filteredProducts = (Page<HomeProductDto>) session.getAttribute("filteredProducts");
            model.addAttribute("products", filteredProducts);
            session.removeAttribute("filteredProducts");
        } else {
            Page<ProductWithOfferDto> products = productService.getAllProductsAsHomeProductDto(pageable);
            model.addAttribute("products", products);
        }

        if (principal != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

            if (roles.contains("ADMIN")) {
                return "redirect:/admin/dashboard";
            }
            if (roles.contains("USER")){
                model.addAttribute("user",userInfoService.currentUserDetail());
            } else {
                model.addAttribute("user",userInfoService.currentUserDetail());
            }

        }

        return "user/product-list";
    }

    @PostMapping("/products/search")
    public String searchProduct(@RequestParam String query, HttpSession session, @PageableDefault(size = 8) Pageable pageable) {
        Page<ProductWithOfferDto> products = productService.searchProductsFor(query, pageable);
        session.setAttribute("SearchProducts", products);
        return "redirect:/products";
    }

    @PostMapping("/products/filter")
    public String filterProducts(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "brandId", required = false) Long brandId,
            HttpSession session,
            @PageableDefault(size = 8) Pageable pageable) {

        Page<ProductWithOfferDto> filteredProducts = productService.filterProductsAsHomeProductDto(categoryId, brandId, pageable);

        session.setAttribute("filteredProducts", filteredProducts);

        return "redirect:/products";
    }

    @GetMapping ("/product/user/review")
    public ResponseEntity<String> addProductReview(@RequestParam("productId") Long productId,
                                                   @RequestParam("rating") int rating,
                                                   @RequestParam("comment") String comment) {
        String userEmail = userInfoService.currentUser();
        UserInfo userInfo = userInfoService.getUserByEmail(userEmail);
        UUID userId = null;
        if (userInfo!=null){
            userId = userInfo.getUserId();
        }

        productReviewService.addProductReview(userId,productId,rating,comment);

        if(productReviewService.isAlreadyExist(userId,productId)){
            return ResponseEntity.ok("Review update successful.");
        }

        return ResponseEntity.ok("Review posted.");
    }



}