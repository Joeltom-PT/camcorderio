package com.camcorderio.camcorderio.service.user;

import com.camcorderio.camcorderio.entity.user.Orders;
import com.camcorderio.camcorderio.exceptions.InsufficientStockException;
import com.camcorderio.camcorderio.exceptions.InsufficientWalletBalanceException;
import com.camcorderio.camcorderio.model.admin.AdminOrderDetailDto;
import com.camcorderio.camcorderio.model.user.CartItemDTO;
import com.camcorderio.camcorderio.model.user.OrderDTO;
import com.camcorderio.camcorderio.model.user.OrderDetailDto;
import com.camcorderio.camcorderio.model.user.OrderItemDto;
import com.razorpay.RazorpayException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    void placeOrderByCOD(Long selectedAddressId, String selectedPaymentMethod, Long couponAmount) throws InsufficientStockException;

    void placeOrderByRazorpay(String paymentId, Long selectedAddressId, String selectedPaymentMethod, Long couponAmount) throws InsufficientStockException;

    void placeOrderByWallet(Long selectedAddressId, String selectedPaymentMethod, Long couponAmount) throws InsufficientWalletBalanceException, InsufficientStockException;

    public List<OrderDTO> getUserOrdersDTOByEmail(String userEmail);

    OrderDetailDto getOrderDetails(long orderId, String email);

    Page<Orders> getAllOrders(Pageable pageable);

    Optional<AdminOrderDetailDto> getOrder(long orderId);


    Orders getOrderByIdAndEmail(long orderId, String email);

    void cancelOrder(long orderId, String userEmail);

    List<OrderItemDto> orderItemsByOrderId(Long orderId);

    void updateOrderDetails(AdminOrderDetailDto orderDetailDto);

    String generateRazorpayOrder(List<CartItemDTO> cartedProducts) throws RazorpayException;


    List<OrderDetailDto> getAllOrderDetails(String email);

    Orders getOrderById(Long id);

    void placeOrderByRazorpayFailed(String orderId, Long selectedAddressId, String selectedPaymentMethod, Long couponAmount);

    String generateRazorpayOrderForFailed(long orderId) throws RazorpayException;

    void successFailedOrder(long orderId);

    boolean removeFailedOrder(Long orderId);
}