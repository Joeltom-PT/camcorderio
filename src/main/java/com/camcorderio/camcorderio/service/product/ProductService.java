package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.model.product.HomeProductDto;
import com.camcorderio.camcorderio.model.product.ProductDto;
import com.camcorderio.camcorderio.model.product.ProductMiniDto;
import com.camcorderio.camcorderio.model.product.ProductWithOfferDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    Product addProduct(ProductDto productDto, List<String> images);

    List<Product> getAllProducts();

    Product getProductById(long id);

    boolean brandExist(long id);

    boolean categoryExist(long id);

    void editProduct(ProductDto productDto, List <MultipartFile> image);

    void deleteProductById(long id);

    List<ProductWithOfferDto> getProductsForHome();

     boolean isProductInWishlist(long productId, String email);

    boolean isProductNameExists(String productName);


    Page<ProductWithOfferDto> getAllProductsAsHomeProductDto(Pageable pageable);

    Page<ProductWithOfferDto> searchProductsFor(String query, Pageable pageable);

    Page<ProductWithOfferDto> filterProductsAsHomeProductDto(Long categoryId, Long brandId, Pageable pageable);

    void updateProductStock(Product product);

    List<ProductMiniDto> getAllProductsWithIdAndName();

    List<ProductDto> getTop10SellingProducts();

    ProductWithOfferDto getProductByIdForDetailPage(long productId);
}