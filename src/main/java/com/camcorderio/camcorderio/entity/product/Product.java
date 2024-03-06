package com.camcorderio.camcorderio.entity.product;

import com.camcorderio.camcorderio.entity.user.WishList;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private Double price;
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT",length = 1000)
    private String description;

    @Lob
    @Column(columnDefinition = "TEXT",length = 1000)
    private String specifications;


    private Integer stock;

    private boolean status = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_path")
    private List<String> imagesPath;

    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Categories category;

    @ManyToOne
    @JoinColumn(name = "brandId")
    private Brands brand;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime publishedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "products")
    private List<WishList> wishLists;

    private boolean isDeleted;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductReview> reviews;

    private Double offerDiscount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime offerCreatedAt;

    private LocalDateTime offerExpireDateTime;


}
