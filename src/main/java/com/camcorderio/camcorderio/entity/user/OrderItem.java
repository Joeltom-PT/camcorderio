package com.camcorderio.camcorderio.entity.user;


import com.camcorderio.camcorderio.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    private Product product;

    private Integer quantity ;

    @ManyToOne()
    private Orders orders;


    public OrderItem(CartItem cartItem) {
        this.product = cartItem.getProduct();
        this.quantity = cartItem.getQuantity();

    }


}
