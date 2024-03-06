package com.camcorderio.camcorderio.entity.user;

import com.camcorderio.camcorderio.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID wishListId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo user;

    @ManyToMany
    @JoinTable(
            name = "wishlist_products",
            joinColumns = @JoinColumn(name = "wishlist_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;

    @CreationTimestamp
    private LocalDateTime createdOn;

}
