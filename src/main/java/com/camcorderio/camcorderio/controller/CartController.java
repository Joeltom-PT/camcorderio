package com.camcorderio.camcorderio.controller;

import com.camcorderio.camcorderio.service.product.CartService;
import com.camcorderio.camcorderio.service.user.UserInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CartController {

    private final CartService cartService;

    private final UserInfoService userInfoService;
    public CartController(CartService cartService, UserInfoService userInfoService) {
        this.cartService = cartService;
        this.userInfoService = userInfoService;
    }

    @GetMapping("/add/to/cart")
    public ResponseEntity<Map<String, String>> addAndUpdateCart(@RequestParam("productId") Long productId) {
        String email = userInfoService.currentUser();
        cartService.addOrUpdateProductInCart(email, productId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "success");
        return ResponseEntity.ok().body(response);
    }



    @GetMapping("/up/to/cart")
    public ResponseEntity<String> increaseQuantity(@RequestParam("productId") Long productId){
        String email = userInfoService.currentUser();
        return cartService.increaseCartQuantity(email,productId);
    }

    @GetMapping("/down/to/cart")
    public ResponseEntity<String> decreaseQuantity(@RequestParam("productId") Long productId){
        String email = userInfoService.currentUser();
        return cartService.decreaseQuantity(email,productId);
    }

    @GetMapping("/remove/cart/item")
    public ResponseEntity<String> removeCartItem(@RequestParam("productId") Long productId){
        String email = userInfoService.currentUser();
        return cartService.removeCartItem(email,productId);
    }

}