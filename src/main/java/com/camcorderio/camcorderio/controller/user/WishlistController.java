package com.camcorderio.camcorderio.controller.user;

import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.entity.user.UserInfo;

import com.camcorderio.camcorderio.service.product.ProductService;
import com.camcorderio.camcorderio.service.user.UserInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    private final UserInfoService userService;
    private final ProductService productService;

    public WishlistController(UserInfoService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping("/add/{productId}")
    public ResponseEntity<String> addToWishlist(@PathVariable Long productId) {
        try {
            String email = userService.currentUser();
            UserInfo user = userService.getUserByEmail(email);
            Product product = productService.getProductById(productId);

            userService.addToWishlist(user, product);

            return ResponseEntity.ok("Product added to wishlist successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error adding product to wishlist");
        }
    }

    @GetMapping("/remove/{productId}")
    public ResponseEntity<String> removeFromWishlist(@PathVariable Long productId) {
        try {
            String email = userService.currentUser();
            UserInfo user = userService.getUserByEmail(email);
            Product product = productService.getProductById(productId);

            userService.removeFromWishlist(user, product);

            return ResponseEntity.ok("Product removed from wishlist successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error removing product from wishlist");
        }
    }
}