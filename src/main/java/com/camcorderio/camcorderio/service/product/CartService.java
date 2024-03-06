package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.entity.product.Categories;
import com.camcorderio.camcorderio.entity.user.Cart;
import com.camcorderio.camcorderio.entity.user.CartItem;
import com.camcorderio.camcorderio.model.user.CartItemDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface CartService {
    public ResponseEntity<String> addOrUpdateProductInCart(String email, Long productId);

    public ResponseEntity<String> increaseCartQuantity(String email, Long productId);

    List<CartItemDTO> getAllCartItems(String email);

    public ResponseEntity<String> decreaseQuantity(String email, Long productId);

    public ResponseEntity<String> removeCartItem(String email, Long productId);

    public Cart getCart(String email);
    public boolean hasCartItemsWithQuantityGreaterThanZero(String userEmail);

}