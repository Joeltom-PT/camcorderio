package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.user.Cart;
import com.camcorderio.camcorderio.entity.user.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    @Query("SELECT ci FROM CartItem ci WHERE ci.product.productId = :productId")
    List<CartItem> findByProductId(@Param("productId") long productId);

    List<CartItem> findByCartAndQuantityGreaterThan(Cart cart, int i);
}
