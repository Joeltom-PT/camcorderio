package com.camcorderio.camcorderio.entity.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categories {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cat_sequence")
    @SequenceGenerator(name = "cat_sequence", sequenceName = "cat_sequence", allocationSize = 1)
    private Long categoryId;
    private String categoryName;
    private String categoryDescription;
    private boolean status = true;
    @Column(name = "is_deleted")
    private boolean isDeleted;
}
