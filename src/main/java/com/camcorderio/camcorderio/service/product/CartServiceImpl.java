package com.camcorderio.camcorderio.service.product;

import com.camcorderio.camcorderio.entity.product.BrandOffer;
import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.entity.user.Cart;
import com.camcorderio.camcorderio.entity.user.CartItem;
import com.camcorderio.camcorderio.entity.user.UserInfo;
import com.camcorderio.camcorderio.exceptions.CartNotFoundException;
import com.camcorderio.camcorderio.model.user.CartItemDTO;
import com.camcorderio.camcorderio.repository.BrandOfferRepository;
import com.camcorderio.camcorderio.repository.CartItemRepository;
import com.camcorderio.camcorderio.repository.CartRepository;
import com.camcorderio.camcorderio.repository.UserInfoRepository;
import com.camcorderio.camcorderio.service.user.UserInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserInfoRepository userInfoRepository;

    private final ProductService productService;

    private final BrandOfferRepository brandOfferRepository;

    private final UserInfoService userInfoService;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository, UserInfoRepository userInfoRepository, ProductService productService, BrandOfferRepository brandOfferRepository, UserInfoService userInfoService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userInfoRepository = userInfoRepository;
        this.productService = productService;
        this.brandOfferRepository = brandOfferRepository;
        this.userInfoService = userInfoService;
    }

    @Override
    @Transactional
    public ResponseEntity<String> addOrUpdateProductInCart(String email, Long productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }

        Cart cart = cartRepository.findByUserEmail(email);

        if (cart != null) {
            updateCart(cart, product);
            return ResponseEntity.status(HttpStatus.OK).body("Product added/updated in the cart successfully.");
        } else {
            return createNewCart(email, product);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> increaseCartQuantity(String email, Long productId) {
        Cart cart = getCartByEmailAndProductId(email, productId);

        Optional<CartItem> cartItemOptional = findCartItemByProductId(cart, productId);

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            calculateAndUpdateTotalPrice(cart);
            cartRepository.save(cart);
            return ResponseEntity.status(HttpStatus.OK).body("Cart quantity increased successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found in the cart.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> decreaseQuantity(String email, Long productId) {
        Cart cart = getCartByEmailAndProductId(email, productId);

        Optional<CartItem> cartItemOptional = findCartItemByProductId(cart, productId);

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            int newQuantity = cartItem.getQuantity() - 1;

            if (newQuantity > 0) {
                increaseProductStock(cartItem.getProduct(), 1);
                cartItem.setQuantity(newQuantity);
                calculateAndUpdateTotalPrice(cart);
                cartRepository.save(cart);
                return ResponseEntity.status(HttpStatus.OK).body("Cart quantity decreased successfully.");
            } else {
                decreaseProductStock(cartItem.getProduct(), cartItem.getQuantity());
                cart.getCartItems().remove(cartItem);
                calculateAndUpdateTotalPrice(cart);
                cartRepository.save(cart);
                return ResponseEntity.status(HttpStatus.OK).body("Product removed from the cart.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found in the cart.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> removeCartItem(String email, Long productId) {
        Cart cart = getCartByEmailAndProductId(email, productId);

        Optional<CartItem> cartItemOptional = findCartItemByProductId(cart, productId);

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            cart.getCartItems().remove(cartItem);
            calculateAndUpdateTotalPrice(cart);
            cartRepository.save(cart);

            return ResponseEntity.status(HttpStatus.OK).body("Product removed from the cart.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found in the cart.");
        }
    }

    @Override
    @Transactional
    public Cart getCart(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        UserInfo user = userInfoRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found for email: " + email);
        }

        return cartRepository.findByUserEmail(email);
    }

    @Override
    @Transactional
    public List<CartItemDTO> getAllCartItems(String email) {
        Cart cart = cartRepository.findByUserEmail(email);

        if (cart != null) {
            List<CartItemDTO> cartItemDTOList = new ArrayList<>();
            List<CartItem> cartItems = cart.getCartItems();

            for (CartItem cartItem : cartItems) {
                CartItemDTO cartItemDTO = new CartItemDTO();
                cartItemDTO.setCartItemId(cartItem.getCartItemId());
                cartItemDTO.setProductId(cartItem.getProduct().getProductId());
                cartItemDTO.setProductName(cartItem.getProduct().getName());
                cartItemDTO.setProductImage(cartItem.getProduct().getImagesPath().get(0));
                cartItemDTO.setProductPrice(cartItem.getProduct().getPrice());
                cartItemDTO.setQuantity(cartItem.getQuantity());

                double offerAmount = 0.0;
                if (cartItem.getProduct().getOfferDiscount() != null) {
                    offerAmount = cartItem.getProduct().getOfferDiscount();
                } else {
                    BrandOffer brandOffer = brandOfferRepository.findByBrandsBrandId(cartItem.getProduct().getBrand().getBrandId());

                    if (brandOffer != null) {
                        double offerPrice = cartItem.getProduct().getPrice() * (brandOffer.getDiscount() / 100.0);
                        offerAmount = offerPrice;
                    }
                }
                cartItemDTO.setOfferAmount(offerAmount);

                cartItemDTOList.add(cartItemDTO);
            }

            return cartItemDTOList;
        } else {
            throw new CartNotFoundException("Cart not found for user with email: " + email);
        }
    }




    @Override
    public boolean hasCartItemsWithQuantityGreaterThanZero(String userEmail) {
        UserInfo userInfo = userInfoService.getUserByEmail(userEmail);
        if (userInfo != null && userInfo.getCart() != null) {
            List<CartItem> cartItems = cartItemRepository.findByCartAndQuantityGreaterThan(userInfo.getCart(), 0);
            return !cartItems.isEmpty();
        } else {
            return false;
        }
    }

    private Cart getCartByEmailAndProductId(String email, Long productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found.");
        }

        Cart cart = cartRepository.findByUserEmail(email);
        if (cart == null) {
            throw new CartNotFoundException("Cart not found for user with email: " + email);
        }

        return cart;
    }

    private boolean isProductAvailable(Product product, int existingQuantity, int quantityToCheck) {
        return product.getStock() >= quantityToCheck + existingQuantity;
    }

    private void updateCart(Cart cart, Product product) {
        Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getProduct().getProductId().equals(product.getProductId()))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            if (cartItem.getQuantity() <= 4) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
            } else {
            }
        } else {
            CartItem newCartItem = new CartItem(cart, product, 1);
            cart.addCartItem(newCartItem);
        }
        calculateAndUpdateTotalPrice(cart);
        cartRepository.save(cart);
    }

    private ResponseEntity<String> createNewCart(String email, Product product) {
        UserInfo user = userInfoRepository.findByEmail(email);

        Cart newCart = new Cart();
        newCart.setUser(user);

        CartItem newCartItem = new CartItem(newCart, product, 1);
        newCart.addCartItem(newCartItem);

        calculateAndUpdateTotalPrice(newCart);
        cartRepository.save(newCart);

        return ResponseEntity.status(HttpStatus.OK).body("New cart created with the product.");
    }

    private void calculateAndUpdateTotalPrice(Cart cart) {
        double totalPrice = cart.getCartItems().stream()
                .mapToDouble(cartItem -> {
                    Long productId = cartItem.getProduct().getProductId();
                    Product product = productService.getProductById(productId);
                    Double price = (product != null) ? product.getPrice() : 0.0;
                    return price * cartItem.getQuantity();
                })
                .sum();
        cart.setTotalPrice(totalPrice);
    }

    private Optional<CartItem> findCartItemByProductId(Cart cart, Long productId) {
        return cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getProduct().getProductId().equals(productId))
                .findFirst();
    }

    private void decreaseProductStock(Product product, int quantity) {
        int currentStock = product.getStock();
        if (currentStock >= quantity) {
            product.setStock(currentStock - quantity);
        } else {
            throw new RuntimeException("Insufficient stock for product: " + product.getProductId());
        }
    }

    private void increaseProductStock(Product product, int quantity) {
        int currentStock = product.getStock();
        product.setStock(currentStock + quantity);
    }
}