package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.entity.product.BrandOffer;
import com.camcorderio.camcorderio.entity.product.Brands;
import com.camcorderio.camcorderio.entity.product.Categories;
import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.entity.user.CartItem;
import com.camcorderio.camcorderio.entity.user.UserInfo;
import com.camcorderio.camcorderio.entity.user.WishList;
import com.camcorderio.camcorderio.model.mapper.ProductMapper;
import com.camcorderio.camcorderio.model.product.HomeProductDto;
import com.camcorderio.camcorderio.model.product.ProductDto;
import com.camcorderio.camcorderio.model.product.ProductMiniDto;
import com.camcorderio.camcorderio.model.product.ProductWithOfferDto;
import com.camcorderio.camcorderio.repository.CartItemRepository;
import com.camcorderio.camcorderio.repository.OrderItemRepository;
import com.camcorderio.camcorderio.repository.ProductRepository;
import com.camcorderio.camcorderio.repository.UserInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final CartItemRepository cartItemRepository;

    private final UserInfoRepository userInfoRepository;

    private final OrderItemRepository orderItemRepository;

    private final BrandOfferService brandOfferService;


    public ProductServiceImpl(ProductRepository productRepository, CartItemRepository cartItemRepository, UserInfoRepository userInfoRepository, OrderItemRepository orderItemRepository, BrandOfferService brandOfferService) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.userInfoRepository = userInfoRepository;
        this.orderItemRepository = orderItemRepository;
        this.brandOfferService = brandOfferService;
    }

    @Override
    public List<Product> getAllProducts(){
        List<Product> productList = productRepository.findAllByIsDeletedFalse();
        return productList;
    }

    @Override
    public Product getProductById(long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
    }

    @Override
    public Product addProduct(ProductDto productDto, List<String> images) {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setSpecifications(productDto.getSpecifications());
        product.setPrice(productDto.getPrice());
        product.setStock(productDto.getStock());
        product.setStatus(productDto.isStatus());
        Categories category = new Categories();
        category.setCategoryId(productDto.getCategoryId());
        product.setCategory(category);
        Brands brand = new Brands();
        brand.setBrandId(productDto.getBrandId());
        product.setBrand(brand);
        product.setImagesPath(images);
        product = productRepository.save(product);
        return product;
    }

    @Override
    public void editProduct(ProductDto productDto, List<MultipartFile> images) {
        Long productId = productDto.getProductId();

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setSpecifications(productDto.getSpecifications());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setStock(productDto.getStock());
        existingProduct.setStatus(productDto.isStatus());

        Categories category = new Categories();
        category.setCategoryId(productDto.getCategoryId());
        existingProduct.setCategory(category);

        Brands brand = new Brands();
        brand.setBrandId(productDto.getBrandId());
        existingProduct.setBrand(brand);

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = saveImages(productId, images);
            existingProduct.setImagesPath(imageUrls);
        }

        existingProduct.setUpdatedAt(LocalDateTime.now());

        productRepository.save(existingProduct);
    }

    @Override
    public void deleteProductById(long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        List<CartItem> cartItems = cartItemRepository.findByProductId(id);
        cartItemRepository.deleteAll(cartItems);

        product.setDeleted(true);
        productRepository.save(product);
    }

    public List<ProductWithOfferDto> getProductsForHome() {
        List<Product> products = productRepository.findTop8ByStatusTrueAndBrandStatusTrueAndCategoryStatusTrueAndIsDeletedFalseOrderByPublishedAtDesc();

        return products.stream()
                .map(this::mapProductToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductWithOfferDto> filterProductsAsHomeProductDto(Long categoryId, Long brandId, Pageable pageable) {
        Page<Product> filteredProducts = productRepository.findByCategoryCategoryIdAndBrandBrandIdAndIsDeletedFalse(categoryId, brandId, pageable);
        List<ProductWithOfferDto> dtos = filteredProducts.getContent().stream()
                .map(this::mapProductToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, filteredProducts.getTotalElements());
    }


    @Override
    @Transactional
    public void updateProductStock(Product product) {

        Optional<Product> existingProductOptional = productRepository.findById(product.getProductId());

        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            existingProduct.setStock(product.getStock());
            productRepository.save(existingProduct);
        } else {
            throw new EntityNotFoundException("Product not found with ID: " + product.getProductId());
        }
    }


    @Override
    public List<ProductMiniDto> getAllProductsWithIdAndName() {
        List<Product> allProducts = productRepository.findAllByIsDeletedFalse();

        List<ProductMiniDto> filteredProducts = allProducts.stream()
                .filter(product -> product.getOfferDiscount() == null)
                .map(product -> new ProductMiniDto(product.getProductId(), product.getName()))
                .collect(Collectors.toList());

        return filteredProducts;
    }


    @Override
    public List<ProductDto> getTop10SellingProducts() {
        List<Object[]> topSellingProducts = orderItemRepository.findTop10SellingProducts();

        return topSellingProducts.stream()
                .map(result -> {
                    Product product = (Product) result[0];
                    Long totalSold = (Long) result[1];
                    return convertToProductDto(product, totalSold);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ProductWithOfferDto getProductByIdForDetailPage(long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            return mapProductToDto(product);
        } else {
            return null;
        }
    }

    @Override
    public boolean isProductNameExists(String productName) {
        return productRepository.findByNameIgnoreCaseAndIsDeletedFalse(productName).isPresent();

    }

    @Override
    public Page<ProductWithOfferDto> getAllProductsAsHomeProductDto(Pageable pageable) {
        Page<Product> products = productRepository.findByStatusTrueAndCategoryStatusTrueAndBrandStatusTrueAndIsDeletedFalse(pageable);
        List<ProductWithOfferDto> dtos = products.stream()
                .map(this::mapProductToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, products.getTotalElements());
    }


    @Override
    public Page<ProductWithOfferDto> searchProductsFor(String query, Pageable pageable) {
        Page<Product> filteredProducts = productRepository.searchProducts(query, query, query, pageable);
        List<ProductWithOfferDto> dtos = filteredProducts.getContent().stream()
                .map(this::mapProductToDtoByPage)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, filteredProducts.getTotalElements());
    }


    @Override
    public boolean brandExist(long id) {
        List<Product> filteredProducts = productRepository.findByBrandBrandIdAndIsDeletedFalse(id);
        return areProductsNotDeleted(filteredProducts);
    }

    @Override
    public boolean categoryExist(long id) {
        List<Product> filteredProducts = productRepository.findByCategoryCategoryIdAndIsDeletedFalse(id);
        return areProductsNotDeleted(filteredProducts);
    }

    @Override
    public boolean isProductInWishlist(long productId, String email) {
        UserInfo user = userInfoRepository.findByEmail(email);
        if (user == null) {
            return false;
        }

        Optional<WishList> wishList = user.getWishLists().stream()
                .filter(wl -> wl.getProducts().stream().anyMatch(p -> p.getProductId() == productId))
                .findFirst();

        return wishList.isPresent();
    }

    private boolean areProductsNotDeleted(List<Product> products) {
        return products.stream().anyMatch(product -> !product.isDeleted());
    }

    public List<String> saveImages(Long productId, List<MultipartFile> images) {
        List<String> imageUrls = new ArrayList<>();

//        for (MultipartFile image : images) {
//            if (image != null && !image.isEmpty()) {
//                try {
//                    String imageUrl = cloudinaryService.uploadImage(image);
//                    imageUrls.add(imageUrl);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        return imageUrls;
    }

    private HomeProductDto mapToHomeProductDto(Product product) {
        HomeProductDto homeProductDto = new HomeProductDto();
        homeProductDto.setProductId(product.getProductId());
        homeProductDto.setName(product.getName());
        homeProductDto.setDescription(product.getDescription());
        homeProductDto.setImagePath(product.getImagesPath().isEmpty() ? null : product.getImagesPath().get(0));
        homeProductDto.setPrice(product.getPrice());
        return homeProductDto;
    }

    private ProductDto convertToProductDto(Product product, Long totalSold) {
        ProductDto productDto = new ProductDto();
        productDto.setProductId(product.getProductId());
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setSpecifications(product.getSpecifications());
        productDto.setPrice(product.getPrice());
        productDto.setStock(product.getStock());
        productDto.setStatus(product.isStatus());
        productDto.setCategoryId(product.getCategory().getCategoryId());
        productDto.setImagesPath(product.getImagesPath());
        productDto.setBrandId(product.getBrand().getBrandId());
        productDto.setTotalSold(totalSold);
        return productDto;
    }


    private ProductWithOfferDto mapProductToDto(Product product) {
        BrandOffer brandOffer = brandOfferService.findBrandOfferByBrandId(product.getBrand().getBrandId());
        return ProductMapper.mapProductToDto(product, brandOffer);
    }

    private ProductWithOfferDto mapProductToDtoByPage(Product product) {
        Hibernate.initialize(product.getImagesPath());
        BrandOffer brandOffer = brandOfferService.findBrandOfferByBrandId(product.getBrand().getBrandId());
        return ProductMapper.mapProductToDto(product, brandOffer);
    }

}