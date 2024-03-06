package com.camcorderio.camcorderio.entity.product;

import com.camcorderio.camcorderio.entity.user.UserInfo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo user;

    @Column(columnDefinition = "TEXT")
    private String review;

    private int rating;

    @CreationTimestamp
    private LocalDateTime createdAt;


}
