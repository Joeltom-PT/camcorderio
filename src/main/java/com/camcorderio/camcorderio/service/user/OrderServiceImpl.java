package com.camcorderio.camcorderio.service.user;

import com.camcorderio.camcorderio.config.RazorpayConfig;
import com.camcorderio.camcorderio.entity.product.BrandOffer;
import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.entity.user.*;
import com.camcorderio.camcorderio.exceptions.InsufficientStockException;
import com.camcorderio.camcorderio.exceptions.InsufficientWalletBalanceException;
import com.camcorderio.camcorderio.exceptions.OrderNotFoundException;
import com.camcorderio.camcorderio.model.admin.AdminOrderDetailDto;
import com.camcorderio.camcorderio.model.mapper.OrderMapper;
import com.camcorderio.camcorderio.model.user.*;
import com.camcorderio.camcorderio.repository.*;
import com.camcorderio.camcorderio.service.product.ProductService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService{

    private final UserInfoService userInfoService;

    private final ProductRepository productRepository;

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    private final BrandOfferRepository brandOfferRepository;
    private final AddressService addressService;

    private final WalletRepository walletRepository;

    private final CouponService couponService;

    private final WalletHistoryRepository walletHistoryRepository;
    private final ProductService productService;

    private final CartRepository cartRepository;

    private final OrderRepository orderRepository;

    private final PaymentsRepository paymentsRepository;

    private final OrderItemRepository orderItemRepository;

    public OrderServiceImpl(UserInfoService userInfoService, ProductRepository productRepository, RazorpayClient razorpayClient, RazorpayConfig razorpayConfig, BrandOfferRepository brandOfferRepository, AddressService addressService, WalletRepository walletRepository, CouponService couponService, WalletHistoryRepository walletHistoryRepository, ProductService productService, CartRepository cartRepository, OrderRepository orderRepository, PaymentsRepository paymentsRepository, OrderItemRepository orderItemRepository) {
        this.userInfoService = userInfoService;
        this.productRepository = productRepository;
        this.razorpayClient = razorpayClient;
        this.razorpayConfig = razorpayConfig;
        this.brandOfferRepository = brandOfferRepository;
        this.addressService = addressService;
        this.walletRepository = walletRepository;
        this.couponService = couponService;
        this.walletHistoryRepository = walletHistoryRepository;
        this.productService = productService;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.paymentsRepository = paymentsRepository;
        this.orderItemRepository = orderItemRepository;
    }

    //COD
    @Override
    public void placeOrderByCOD(Long selectedAddressId, String selectedPaymentMethod, Long couponAmount) throws InsufficientStockException {
        String userEmail = userInfoService.currentUser();
        UserInfo user = userInfoService.getUserByEmail(userEmail);
        Address selectedAddress = addressService.findById(selectedAddressId);
        int discount = 0;

        if (couponAmount != null && couponAmount > 0) {
            discount = couponAmount.intValue();
        }

        Orders order = new Orders();

        order.setOrderDate(LocalDate.now());
        order.setUser(user);
        order.setAddress(selectedAddress);
        order.setCouponDiscount(discount);
        double totalAmount = calculateTotalAmount(user.getCart().getCartItems());

        double discountedAmount = totalAmount - discount;
        double finalAmount = Math.max(0, discountedAmount);

        order.setTotalAmount(finalAmount);
        order.setAmountStatus("Pending");
        order.setDeliveredDate(null);
        order.setReturnExpiryDate(null);
        order.setCancelled(false);
        order.setRefundStatus(false);
        order.setStatus("Processing");

        Payments payments = new Payments();
        payments.setPaymentMethod(PaymentMethod.valueOf(selectedPaymentMethod));
        payments.setStatus("Pending");
        payments.setPaymentTime(null);
        payments.setAmount(finalAmount);
        payments.setOrders(order);
        order.setPayments(payments);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : user.getCart().getCartItems()) {
            OrderItem orderItem = new OrderItem(cartItem);
            orderItem.setOrders(order);
            orderItems.add(orderItem);

            Product product = cartItem.getProduct();
            int currentStock = product.getStock();
            int orderedQuantity = cartItem.getQuantity();
            if (currentStock >= orderedQuantity) {
                int newStock = currentStock - orderedQuantity;
                product.setStock(newStock);
                productService.updateProductStock(product);
            } else {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
        }
        order.setOrderProducts(orderItems);

        orderRepository.save(order);

        user.getCart().getCartItems().clear();
        user.getCart().clear();
        cartRepository.save(user.getCart());
    }



    // Razorpay
    @Override
    public void placeOrderByRazorpay(String paymentId, Long selectedAddressId, String selectedPaymentMethod, Long couponAmount) throws InsufficientStockException {
        String userEmail = userInfoService.currentUser();
        UserInfo user = userInfoService.getUserByEmail(userEmail);
        Address selectedAddress = addressService.findById(selectedAddressId);

        int discount = 0;

        if (couponAmount != null && couponAmount > 0) {
            discount = couponAmount.intValue();
        }

        Orders order = new Orders();

        order.setOrderDate(LocalDate.now());
        order.setUser(user);
        order.setAddress(selectedAddress);
        order.setCouponDiscount(discount);
        double totalAmount = calculateTotalAmount(user.getCart().getCartItems());

        double discountedAmount = totalAmount - discount;
        double finalAmount = Math.max(0, discountedAmount);

        order.setTotalAmount(finalAmount);
        order.setAmountStatus("Paid");
        order.setDeliveredDate(null);
        order.setReturnExpiryDate(null);
        order.setCancelled(false);
        order.setRefundStatus(false);
        order.setStatus("Processing");

        Payments payments = new Payments();
        payments.setPaymentMethod(PaymentMethod.valueOf(selectedPaymentMethod));
        payments.setStatus("Paid");
        payments.setPaymentTime(String.valueOf(LocalDateTime.now()));

       double lastAmount = order.getTotalAmount() - (couponAmount != null ? couponAmount : 0);

        lastAmount = Math.max(lastAmount, 0);
        payments.setAmount(lastAmount);

        payments.setOrders(order);
        order.setPayments(payments);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : user.getCart().getCartItems()) {
            OrderItem orderItem = new OrderItem(cartItem);
            orderItem.setOrders(order);
            orderItems.add(orderItem);

            Product product = cartItem.getProduct();
            int currentStock = product.getStock();
            int orderedQuantity = cartItem.getQuantity();
            if (currentStock >= orderedQuantity) {
                int newStock = currentStock - orderedQuantity;
                product.setStock(newStock);
                productService.updateProductStock(product);
            } else {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
        }
        order.setOrderProducts(orderItems);

        orderRepository.save(order);

        user.getCart().getCartItems().clear();
        user.getCart().clear();
        cartRepository.save(user.getCart());
    }

    @Override
    public void placeOrderByRazorpayFailed(String orderId, Long selectedAddressId, String selectedPaymentMethod, Long couponAmount) {
        String userEmail = userInfoService.currentUser();
        UserInfo user = userInfoService.getUserByEmail(userEmail);
        Address selectedAddress = addressService.findById(selectedAddressId);

        int discount = 0;

        if (couponAmount != null && couponAmount > 0) {
            discount = couponAmount.intValue();
        }

        Orders order = new Orders();

        order.setOrderDate(LocalDate.now());
        order.setUser(user);
        order.setAddress(selectedAddress);
        order.setCouponDiscount(discount);
        double totalAmount = calculateTotalAmount(user.getCart().getCartItems());

        double discountedAmount = totalAmount - discount;
        double finalAmount = Math.max(0, discountedAmount);

        order.setTotalAmount(finalAmount);
        order.setAmountStatus("Pending");
        order.setDeliveredDate(null);
        order.setReturnExpiryDate(null);
        order.setCancelled(false);
        order.setRefundStatus(false);
        order.setStatus("Pending");

        Payments payments = new Payments();
        payments.setPaymentMethod(PaymentMethod.valueOf(selectedPaymentMethod));
        payments.setStatus("Pending");
        payments.setPaymentTime(String.valueOf(LocalDateTime.now()));

        double lastAmount = order.getTotalAmount() - (couponAmount != null ? couponAmount : 0);

        lastAmount = Math.max(lastAmount, 0);
        payments.setAmount(lastAmount);

        payments.setOrders(order);
        order.setPayments(payments);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : user.getCart().getCartItems()) {
            OrderItem orderItem = new OrderItem(cartItem);
            orderItem.setOrders(order);
            orderItems.add(orderItem);

            Product product = cartItem.getProduct();
            int currentStock = product.getStock();
            int orderedQuantity = cartItem.getQuantity();
            if (currentStock >= orderedQuantity) {
                int newStock = currentStock - orderedQuantity;
                product.setStock(newStock);
                productService.updateProductStock(product);
            }
        }
        order.setOrderProducts(orderItems);

        orderRepository.save(order);

        user.getCart().getCartItems().clear();
        user.getCart().clear();
        cartRepository.save(user.getCart());
    }




    @Override
    public void successFailedOrder(long orderId) {
        Orders order = orderRepository.findById(orderId).get();
        order.setAmountStatus("Paid");
        order.setStatus("Processing");
        orderRepository.save(order);

        Payments payments = paymentsRepository.findById(order.getPayments().getPaymentId()).get();
        payments.setStatus("Paid");
        paymentsRepository.save(payments);

    }

    @Override
    public boolean removeFailedOrder(Long orderId) {
       Orders order = orderRepository.findById(orderId).get();
       order.setCancelled(true);
       order.setStatus("CANCELLED");
       Payments payments = paymentsRepository.findById(order.getPayments().getPaymentId()).get();
       payments.setStatus("CANCELLED");
       orderRepository.save(order);
       return true;
    }


    @Override
    public String generateRazorpayOrderForFailed(long orderId) throws RazorpayException {
        RazorpayClient razorpayClient = razorpayConfig.razorpayClient();
        Orders orders = orderRepository.findById(orderId).get();
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", orders.getTotalAmount() * 100);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", UUID.randomUUID().toString());

        try {
            Order order = razorpayClient.orders.create(orderRequest);
            return order.get("id");
        } catch (RazorpayException e) {
            throw new RazorpayException("Failed to create Razorpay order", e);
        }
    }



    // Wallet
    @Override
    public void placeOrderByWallet(Long selectedAddressId, String selectedPaymentMethod, Long couponAmount) throws InsufficientWalletBalanceException, InsufficientStockException {
        String userEmail = userInfoService.currentUser();
        UserInfo user = userInfoService.getUserByEmail(userEmail);
        Address selectedAddress = addressService.findById(selectedAddressId);

        int discount = 0;

        if (couponAmount != null && couponAmount > 0) {
            discount = couponAmount.intValue();
        }

        Orders order = new Orders();

        order.setOrderDate(LocalDate.now());
        order.setUser(user);
        order.setAddress(selectedAddress);
        order.setCouponDiscount(discount);
        double totalAmount = calculateTotalAmount(user.getCart().getCartItems());

        double discountedAmount = totalAmount - discount;
        double finalAmount = Math.max(0, discountedAmount);

        order.setTotalAmount(finalAmount);
        order.setAmountStatus("Paid");
        order.setDeliveredDate(null);
        order.setReturnExpiryDate(null);
        order.setCancelled(false);
        order.setRefundStatus(false);
        order.setStatus("Processing");

        Wallet wallet = user.getWallet();
        double orderTotalAmount = order.getTotalAmount();

        if (wallet.getTotalAmount() >= orderTotalAmount) {
            wallet.setTotalAmount(wallet.getTotalAmount() - orderTotalAmount);
        } else {
            throw new InsufficientWalletBalanceException("Insufficient wallet balance for the order");
        }

        WalletHistory walletHistory = new WalletHistory();
        walletHistory.setWithdrawAmount(orderTotalAmount);
        walletHistory.setAmountWithdrawTime(LocalDateTime.now().toString());
        walletHistory.setDepositOrWithdraw("Withdraw");
        walletHistory.setWallet(wallet);

        Payments payments = new Payments();
        payments.setPaymentMethod(PaymentMethod.valueOf(selectedPaymentMethod));
        payments.setStatus("Paid");
        payments.setPaymentTime(String.valueOf(LocalDateTime.now()));

        double lastAmount = order.getTotalAmount() - (couponAmount != null ? couponAmount : 0);

        lastAmount = Math.max(lastAmount, 0);
        payments.setAmount(lastAmount);

        payments.setOrders(order);
        order.setPayments(payments);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : user.getCart().getCartItems()) {
            OrderItem orderItem = new OrderItem(cartItem);
            orderItem.setOrders(order);
            orderItems.add(orderItem);

            Product product = cartItem.getProduct();
            int currentStock = product.getStock();
            int orderedQuantity = cartItem.getQuantity();
            if (currentStock >= orderedQuantity) {
                int newStock = currentStock - orderedQuantity;
                product.setStock(newStock);
                productService.updateProductStock(product);
            } else {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
        }
        order.setOrderProducts(orderItems);

        orderRepository.save(order);

        user.getCart().getCartItems().clear();
        user.getCart().clear();
        cartRepository.save(user.getCart());

        walletRepository.save(wallet);
        walletHistoryRepository.save(walletHistory);
    }



    public List<OrderDTO> getUserOrdersDTOByEmail(String userEmail) {
        List<Orders> userOrders = orderRepository.findByUserEmail(userEmail);
        return userOrders.stream().map(OrderDTO::new).collect(Collectors.toList());
    }

    @Override
    public OrderDetailDto getOrderDetails(long orderId, String userEmail) {

        Orders order = orderRepository.findByUserEmailAndOrderId( userEmail, orderId);

        if (order == null) {
            throw new OrderNotFoundException("Order not found");
        }

        return convertToOrderDetailDto(order);
    }

    @Override
    public Page<Orders> getAllOrders(Pageable pageable) {
        return orderRepository.findAllWithoutPendingStatus(pageable);
    }

    @Override
    public Optional<AdminOrderDetailDto> getOrder(long orderId) {
        Optional<Orders> optionalOrder = orderRepository.findById(orderId);

        return optionalOrder.map(OrderMapper::mapToAdminOrderDetailDto);
    }

    @Override
    public Orders getOrderByIdAndEmail(long orderId, String email) {
        return orderRepository.findByOrderIdAndUser_Email(orderId, email);
    }

    @Override
    public void cancelOrder(long orderId, String userEmail) {
        Orders order = orderRepository.findByUserEmailAndOrderId(userEmail, orderId);

        if (order == null) {
            throw new OrderNotFoundException("Order not found");
        }

        Payments payment = order.getPayments();

        if (!order.isCancelled() && !"Delivered".equalsIgnoreCase(order.getStatus()) && !"COD".equalsIgnoreCase(String.valueOf(payment.getPaymentMethod()))) {
            UserInfo user = order.getUser();

            Wallet wallet = user.getWallet();

            for (OrderItem orderItem : order.getOrderProducts()) {
                Product product = orderItem.getProduct();
                int quantityToIncrease = orderItem.getQuantity();
                product.setStock(product.getStock() + quantityToIncrease);
            }

            wallet.setTotalAmount(wallet.getTotalAmount() + order.getTotalAmount());

            WalletHistory walletHistory = new WalletHistory();
            walletHistory.setAddedAmount(order.getTotalAmount());
            walletHistory.setAmountAddedTime(LocalDateTime.now().toString());
            walletHistory.setDepositOrWithdraw("Deposit");
            walletHistory.setWallet(wallet);

            order.setCancelled(true);
            order.setStatus("CANCELLED");

            orderRepository.save(order);
            walletRepository.save(wallet);
            walletHistoryRepository.save(walletHistory);

            for (OrderItem orderItem : order.getOrderProducts()) {
                Product product = orderItem.getProduct();
                productRepository.save(product);
            }
        } else {
            throw new IllegalStateException("Order cannot be cancelled.");
        }
    }

    @Override
    public List<OrderItemDto> orderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrders_OrderId(orderId);
        return orderItems.stream()
                .map(this::convertOrderItemToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateOrderDetails(AdminOrderDetailDto orderDetailDto) {
        Orders order = orderRepository.findById(orderDetailDto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(orderDetailDto.getStatus());

        if (order.getReturnExpiryDate()==null&&orderDetailDto.getDeliveredDate()!=null){
            order.setReturnExpiryDate(orderDetailDto.getDeliveredDate().plusDays(15));
        }

        Payments payment = order.getPayments();
        if (payment != null) {
            payment.setStatus(orderDetailDto.getPaymentStatus());
        }
        order.setDeliveredDate(orderDetailDto.getDeliveredDate());
        order.setCancelled(orderDetailDto.isCancelled());
        order.setRefundStatus(orderDetailDto.isRefundStatus());

        orderRepository.save(order);
    }

    @Override
    public String generateRazorpayOrder(List<CartItemDTO> cartedProducts) throws RazorpayException {
        RazorpayClient razorpayClient = razorpayConfig.razorpayClient();
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", calculateTotalAmountForDTO(cartedProducts) * 100);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", UUID.randomUUID().toString());

        try {
            Order order = razorpayClient.orders.create(orderRequest);
            return order.get("id");
        } catch (RazorpayException e) {
            throw new RazorpayException("Failed to create Razorpay order", e);
        }
    }


    @Override
    public List<OrderDetailDto> getAllOrderDetails(String email) {
        List<Orders> userOrders = orderRepository.findByUserEmail(email);
        return userOrders.stream()
                .map(this::convertToOrderDetailDto)
                .collect(Collectors.toList());
    }

    @Override
    public Orders getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    private double calculateTotalAmountForCartItems(List<CartItem> cartItems) {
        double totalAmount = 0.0;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();
            double productPrice = product.getPrice();

            totalAmount += quantity * productPrice;
        }

        return totalAmount;
    }

    private double calculateTotalAmountForDTO(List<CartItemDTO> cartedProducts) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();

        Long couponId = (Long) session.getAttribute("coupon");

        double totalAmount = 0.0;

        for (CartItemDTO cartItem : cartedProducts) {
            Product product = productService.getProductById(cartItem.getProductId());
            double itemPrice = product.getPrice();

            if (product.getOfferDiscount() != null) {
                itemPrice -= product.getOfferDiscount();
            } else {
                Optional<BrandOffer> brandOffer = brandOfferRepository.findById(product.getBrand().getBrandId());
                if (brandOffer.isPresent()) {
                    double discountAmount = itemPrice * (brandOffer.get().getDiscount() / 100.0);
                    itemPrice -= discountAmount;
                }
            }

            itemPrice = Math.max(itemPrice, 0.0);

            totalAmount += cartItem.getQuantity() * itemPrice;
        }

        if (couponId != null) {
            CouponDto couponDto = couponService.getCouponById(couponId);
            if (couponDto != null) {
                totalAmount -= couponDto.getAmount();
            }
        }
        String email = userInfoService.currentUser();
        Cart cart = cartRepository.findByUserEmail(email);
        if (totalAmount < 1) {
            totalAmount = cart.getTotalPrice();
        }

        if (totalAmount > 1000){
            totalAmount += 40;
        }

        return totalAmount;
    }


    private double calculateTotalAmountFor(List<CartItemDTO> cartedProducts) {
        double totalAmount = 0.0;

        for (CartItemDTO cartItemDTO : cartedProducts) {
            double productPrice = cartItemDTO.getProductPrice();
            int quantity = cartItemDTO.getQuantity();

            totalAmount += quantity * productPrice;
        }

        return totalAmount;
    }



    private OrderItemDto convertOrderItemToDto(OrderItem orderItem) {
        OrderItemDto orderItemDto = new OrderItemDto();

        orderItemDto.setProductName(orderItem.getProduct().getName());
        orderItemDto.setQuantity(orderItem.getQuantity());
        orderItemDto.setImageUrl(orderItem.getProduct().getImagesPath().get(0));

        return orderItemDto;
    }

    private double calculateTotalAmount(List<CartItem> cartItems) {
        double totalAmount = 0.0;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();
            double productPrice = product.getPrice();
            double offer = 0.0;

            if (product.getOfferDiscount() != null) {
                offer = product.getOfferDiscount();
            } else {
                Optional<BrandOffer> brandOffer = brandOfferRepository.findById(product.getBrand().getBrandId());
                if (brandOffer.isPresent()) {
                    offer = (brandOffer.get().getDiscount() / 100.0) * productPrice;
                }
            }

            totalAmount += quantity * productPrice;
            totalAmount -= offer;
        }

        if (totalAmount > 1000){
            totalAmount += 40;
        }

        return totalAmount;
    }

    private OrderDetailDto convertToOrderDetailDto(Orders order) {
        OrderDetailDto orderDetailDto = new OrderDetailDto();

        orderDetailDto.setOrderId(order.getOrderId());

        List<OrderItemDto> orderItemsDto = order.getOrderProducts().stream()
                .map(orderItem -> {
                    OrderItemDto orderItemDto = new OrderItemDto();
                    orderItemDto.setProductName(orderItem.getProduct().getName());
                    orderItemDto.setQuantity(orderItem.getQuantity());
                    orderItemDto.setImageUrl(orderItem.getProduct().getImagesPath().get(0));
                    return orderItemDto;
                })
                .collect(Collectors.toList());
        orderDetailDto.setCouponDiscount((int) order.getCouponDiscount());
        orderDetailDto.setOrderItems(orderItemsDto);
        orderDetailDto.setAddress(order.getAddress().getFullAddress());
        orderDetailDto.setOrderDate(order.getOrderDate());
        orderDetailDto.setTotalAmount(order.getTotalAmount());
        orderDetailDto.setPaymentMethod(order.getPayments().getPaymentMethod().getDisplayName());
        orderDetailDto.setPaymentStatus(order.getPayments().getStatus());
        orderDetailDto.setOrderStatus(order.getStatus());
        orderDetailDto.setDeliveredDate(order.getDeliveredDate());
        orderDetailDto.setReturnExpiryDate(order.getReturnExpiryDate());
        orderDetailDto.setOrderTrackingStatuses(getOrderTrackingStatuses(order.getStatus()));

        return orderDetailDto;
    }

    private List<String> getOrderTrackingStatuses(String orderStatus) {
        List<String> trackingStatuses = new ArrayList<>();

        Map<String, List<String>> statusMapping = new HashMap<>();
        statusMapping.put("pending", Arrays.asList("pending", "PROCESSING"));
        statusMapping.put("SHIPPED", Arrays.asList("SHIPPED", "DELIVERED"));
        statusMapping.put("CANCELLED", Collections.singletonList("CANCELLED"));
        statusMapping.put("RETURNED", Collections.singletonList("RETURNED"));

        if (statusMapping.containsKey(orderStatus)) {
            trackingStatuses.addAll(statusMapping.get(orderStatus));
        } else {

            trackingStatuses.add("UNKNOWN_STATUS");
        }

        return trackingStatuses;
    }

}