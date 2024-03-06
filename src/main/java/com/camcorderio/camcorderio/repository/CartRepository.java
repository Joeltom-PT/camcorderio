package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.user.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Cart findByUserEmail(String email);
}
