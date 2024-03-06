package com.camcorderio.camcorderio.controller;

import com.camcorderio.camcorderio.config.RazorpayConfig;
import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.entity.user.*;
import com.camcorderio.camcorderio.exceptions.InsufficientStockException;
import com.camcorderio.camcorderio.exceptions.InsufficientWalletBalanceException;
import com.camcorderio.camcorderio.exceptions.OrderFailedException;
import com.camcorderio.camcorderio.exceptions.OrderNotFoundException;
import com.camcorderio.camcorderio.model.user.AddressDto;
import com.camcorderio.camcorderio.model.user.CartItemDTO;
import com.camcorderio.camcorderio.model.user.CouponDto;
import com.camcorderio.camcorderio.model.user.OrderDetailDto;
import com.camcorderio.camcorderio.repository.BrandOfferRepository;
import com.camcorderio.camcorderio.service.product.CartService;
import com.camcorderio.camcorderio.service.product.ProductService;
import com.camcorderio.camcorderio.service.user.*;
import com.razorpay.RazorpayException;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Controller
public class OrderController {

    private final UserInfoService userInfoService;

    private final CartService cartService;

    private final OrderService orderService;

    private final ReturnRequestService returnRequestService;

    private final RazorpayConfig razorpayConfig;

    private final BrandOfferRepository brandOfferRepository;

    private final ProductService productService;

    private final AddressService addressService;

    private final CouponService couponService;

    public OrderController(UserInfoService userInfoService, CartService cartService, OrderService orderService, ReturnRequestService returnRequestService, RazorpayConfig razorpayConfig, BrandOfferRepository brandOfferRepository, ProductService productService, AddressService addressService, CouponService couponService) {
        this.userInfoService = userInfoService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.returnRequestService = returnRequestService;
        this.razorpayConfig = razorpayConfig;
        this.brandOfferRepository = brandOfferRepository;
        this.productService = productService;
        this.addressService = addressService;
        this.couponService = couponService;
    }

    @GetMapping("/user/checkout")
    public String checkOutPage(Model model, Principal principal, HttpSession session) {
        String accessAllowed = (String) session.getAttribute("checkout");
        Long couponId = (Long) session.getAttribute("coupon");
        CouponDto couponDto = null;
        if (couponId != null) {
            long couponID = couponId.longValue();
            couponDto = couponService.getCouponById(couponId);
        }

        if (accessAllowed == null) {
            return "redirect:/user/cart";
        }
        String email = userInfoService.currentUser();
        UserInfo user = userInfoService.getUserByEmail(email);
        List<AddressDto> addressDtoList = userInfoService.getUserAddressByEmail(principal.getName());
        List<CartItemDTO> cartedProducts = cartService.getAllCartItems(email);
        Cart cart = cartService.getCart(email);
        Double total = 0.0;
        for (CartItemDTO h : cartedProducts) {
            total += (h.getProductPrice() - h.getOfferAmount()) * h.getQuantity();
        }
        cart.setTotalPrice(total);
        boolean showWalletPayment;
        if (user.getWallet() != null && user.getWallet().getTotalAmount() >= cart.getTotalPrice() + 40 && cart.getTotalPrice() > 1000){
             showWalletPayment = true;
        }
        else {
             showWalletPayment = user.getWallet() != null && user.getWallet().getTotalAmount() >= cart.getTotalPrice();
        }

        model.addAttribute("allAddress", addressDtoList);
        model.addAttribute("user", userInfoService.getUserByEmail(principal.getName()));
        model.addAttribute("cart", cart);
        model.addAttribute("cartedProducts", cartedProducts);
        if (couponDto != null) {
            model.addAttribute("coupon", couponDto);
        }
        if (showWalletPayment) {
            model.addAttribute("paymentMethods", PaymentMethod.values());
        } else {
            model.addAttribute("paymentMethods", Arrays.stream(PaymentMethod.values())
                    .filter(method -> method != PaymentMethod.WALLET)
                    .toArray(PaymentMethod[]::new));
        }

        return "user/checkout";
    }

    @GetMapping("/user/add/coupon")
    public ResponseEntity<Map<String, String>> addCouponInOrder(@RequestParam("couponCode") String couponCode, HttpSession session) {
        CouponDto couponDto = couponService.getCouponByCouponCode(couponCode);
        String email = userInfoService.currentUser();
        Cart cart = cartService.getCart(email);
        Map<String, String> responseData = new HashMap<>();

        if (couponDto == null) {
            responseData.put("error", "No coupon found by the provided id. Please try another.");
            return ResponseEntity.ok(responseData);
        }

        if (cart.getTotalPrice() < couponDto.getMinimumOrderAmount()) {
            responseData.put("error", "This coupon requires a minimum order amount of ₹" + couponDto.getMinimumOrderAmount());
            return ResponseEntity.ok(responseData);
        }

        if (couponService.isCouponAlreadyUsedByUser(email, couponDto.getId())) {
            responseData.put("error", "Coupon is already used. Try another one.");
            return ResponseEntity.ok(responseData);
        }

        if (couponService.isCouponExpired(couponDto.getId())) {
            responseData.put("error", "Coupon is expired. Try another one.");
            return ResponseEntity.ok(responseData);
        }

        if (!couponService.isCouponStarted(couponDto.getId())) {
            responseData.put("error", "Coupon starts on " + couponDto.getStartDate() + ". Try another one.");
            return ResponseEntity.ok(responseData);
        }

        session.setAttribute("coupon", couponDto.getId());

        responseData.put("success", "Successfully added the coupon");
        responseData.put("couponDiscount", String.valueOf(couponDto.getAmount()));

        return ResponseEntity.ok(responseData);
    }


    @PostMapping("/user/placeOrder")
    public String placeOrder(@RequestParam(name = "selectedAddress", required = false) Long selectedAddressId,
                             @RequestParam(name = "selectedPaymentMethod", required = false) String selectedPaymentMethod,
                             RedirectAttributes attributes, HttpSession session) {
        boolean error = false;
        if (selectedAddressId == null) {
            attributes.addFlashAttribute("addressError", "Address not selected");
            error = true;
        }
        if (selectedPaymentMethod == null) {
            attributes.addFlashAttribute("paymentError", "Payment method is not selected");
            error = true;
        }
        String email = userInfoService.currentUser();
        Cart cart = cartService.getCart(email);

        if (cart.getTotalPrice() > 1000 && "COD".equals(selectedPaymentMethod)) {
            attributes.addFlashAttribute("codError", "Cash on Delivery only available for orders less than 1000 Rs");
            error = true;
        }


        List<CartItemDTO> cartedProducts = cartService.getAllCartItems(email);

        for (CartItemDTO cartItem : cartedProducts) {
            Product product = productService.getProductById(cartItem.getProductId());
            int availableStock = product.getStock();
            if (availableStock < cartItem.getQuantity()) {
                attributes.addFlashAttribute("quantityError", "Insufficient stock for "
                        + product.getName() + ". Available stock: " + availableStock);
                error = true;
                break;
            }
        }

        if (error == true) {
            return "redirect:/user/checkout";
        }
        session.setAttribute("selectedPaymentMethod", selectedPaymentMethod);
        session.setAttribute("selectedAddressId", selectedAddressId);

        return "redirect:/user/orderConfirmation";
    }

    @GetMapping("/user/orderConfirmation")
    public String orderConfirmationPage(HttpSession session, Model model) {
        Long selectedAddressId = (Long) session.getAttribute("selectedAddressId");
        String selectedPaymentMethod = (String) session.getAttribute("selectedPaymentMethod");
        CouponDto couponDto = null;
        Long couponId = (Long) session.getAttribute("coupon");
        if (couponId != null) {
            long coupon = couponId.longValue();
            couponDto = couponService.getCouponById(coupon);
        }


        if (Objects.isNull(selectedAddressId) || Objects.isNull(selectedPaymentMethod)) {
            return "redirect:/user/checkout";
        }

        String email = userInfoService.currentUser();
        List<CartItemDTO> cartedProducts = cartService.getAllCartItems(email);
        Cart cart = cartService.getCart(email);

        Address address = addressService.findById(selectedAddressId);
        PaymentMethod paymentMethod = PaymentMethod.valueOf(selectedPaymentMethod);

        model.addAttribute("address", address);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("cart", cart);
        model.addAttribute("cartedProducts", cartedProducts);
        if (couponDto != null) {
            model.addAttribute("coupon", couponDto);
        }
        return "user/orderConfirmation";
    }

    @GetMapping("/user/orderConfirmation/Complete")
    public String orderConformationPageSuccess(HttpSession session, RedirectAttributes attributes, Model model) {
        Long selectedAddressId = (Long) session.getAttribute("selectedAddressId");
        String selectedPaymentMethod = (String) session.getAttribute("selectedPaymentMethod");
        Long couponId = (Long) session.getAttribute("coupon");
        CouponDto couponDto = null;
        Long couponAmount = null;
        if (couponId != null) {
            couponDto = couponService.getCouponById(couponId);
            couponAmount = (long) couponDto.getAmount();
        }

        if (selectedPaymentMethod.equals(PaymentMethod.COD.name()) || selectedPaymentMethod.equals(PaymentMethod.WALLET.name())) {
            session.removeAttribute("selectedAddressId");
            session.removeAttribute("selectedPaymentMethod");
        }
        if (selectedAddressId == null || selectedPaymentMethod == null) {
            return "redirect:/user/checkout";
        }

        String userEmail = userInfoService.currentUser();

        if (!userInfoService.isUserAddressAvailableByEmail(userEmail, selectedAddressId)
                || !(selectedPaymentMethod.equals(PaymentMethod.COD.name())
                || selectedPaymentMethod.equals(PaymentMethod.RAZORPAY.name())
                || selectedPaymentMethod.equals(PaymentMethod.WALLET.name()))) {
            attributes.addFlashAttribute("error", "Invalid address or payment method selected");
            return "redirect:/user/checkout";
        }

        List<CartItemDTO> cartedProducts = cartService.getAllCartItems(userEmail);

        try {
            if (selectedPaymentMethod.equals(PaymentMethod.COD.name())) {
                orderService.placeOrderByCOD(selectedAddressId, selectedPaymentMethod, couponAmount);
                if (couponDto != null) {
                    couponService.couponUsed(couponDto.getId(), userEmail);
                    session.removeAttribute("coupon");
                }
                session.setAttribute("orderPlaced", "orderPlaced");
                return "redirect:/user/orderPlaced";
            } else if (selectedPaymentMethod.equals(PaymentMethod.WALLET.name())) {
                orderService.placeOrderByWallet(selectedAddressId, selectedPaymentMethod, couponAmount);
                if (couponDto != null) {
                    couponService.couponUsed(couponDto.getId(), userEmail);
                    session.removeAttribute("coupon");
                }
                session.setAttribute("orderPlaced", "orderPlaced");
                return "redirect:/user/orderPlaced";
            } else if (selectedPaymentMethod.equals(PaymentMethod.RAZORPAY.name())) {
                try {
                    String email = userInfoService.currentUser();
                    Cart cart = cartService.getCart(email);
                    Double orderAmount = cart.getTotalPrice();
                    String razorpayOrderId = orderService.generateRazorpayOrder(cartedProducts);
                    UserInfo user = userInfoService.currentUserDetail();
                    Double totalOrderAmount = orderAmount;
                    if (couponAmount != null) {
                        totalOrderAmount = totalOrderAmount - couponAmount;
                    }

                    totalOrderAmount = Math.max(totalOrderAmount, 0.0);
                    if (totalOrderAmount > 1000){
                        totalOrderAmount += 40;
                    }
                    model.addAttribute("coupon", couponDto);
                    model.addAttribute("razorpayOrderId", razorpayOrderId);
                    model.addAttribute("totalOrderAmount", totalOrderAmount);
                    model.addAttribute("user", user);
                    return "user/razorpayPayment";
                } catch (RazorpayException e) {
                    attributes.addFlashAttribute("error", "Failed to create Razorpay order");
                    return "redirect:/user/checkout";
                }
            }
        } catch (OrderFailedException e) {
            attributes.addFlashAttribute("error", "Failed to place the order");
        } catch (InsufficientStockException e) {
            throw new RuntimeException(e);
        } catch (InsufficientWalletBalanceException e) {
            throw new RuntimeException(e);
        }

        return "redirect:/user/checkout";
    }


    @GetMapping("/user/orderPlaced")
    public String orderPlaced(HttpSession session) {
        Object orderPlacedSuccessfully = session.getAttribute("orderPlaced");
        if (orderPlacedSuccessfully == null) {
            return "redirect:/";
        }
        session.removeAttribute("orderPlaced");
        return "/user/orderPlaced";
    }

    @GetMapping("/user/orderFailed")
    public String orderFailed(HttpSession session) {
        Object orderPlacedSuccessfully = session.getAttribute("orderPlaced");
        if (orderPlacedSuccessfully == null) {
            return "redirect:/";
        }
        session.removeAttribute("orderPlaced");
        return "/user/orderPlacedError";
    }


    @GetMapping("/user/orders")
    public String userOrders(Model model) {
        String email = userInfoService.currentUser();
        List<OrderDetailDto> orderDetailDtos = orderService.getAllOrderDetails(email);
        model.addAttribute("orders", orderDetailDtos);
        model.addAttribute("user", userInfoService.getUserByEmail(email));
        return "user/orders";
    }

    @GetMapping("/user/orderDetails/{id}")
    public String orderDetails(@PathVariable("id") long orderId, Model model) {
        String email = userInfoService.currentUser();
        OrderDetailDto orderDetailDto = orderService.getOrderDetails(orderId, email);
        System.out.println(orderDetailDto);
        if (orderDetailDto.getReturnExpiryDate() != null && orderDetailDto.getReturnExpiryDate().isBefore(LocalDate.now()) || orderDetailDto.getOrderStatus() == "Delivered") {
            model.addAttribute("returnReasons", ReturnReason.values());
        }
        model.addAttribute("orderDetails", orderDetailDto);

        model.addAttribute("orderItems", orderDetailDto.getOrderItems());

        model.addAttribute("orderTrackingStatuses", orderDetailDto.getOrderTrackingStatuses());

        return "user/orderDetails";
    }

    @PostMapping("/user/order/cancel/{id}")
    public String cancelOrder(@PathVariable long id, RedirectAttributes redirectAttributes) {
        String userEmail = userInfoService.currentUser();

        try {
            orderService.cancelOrder(id, userEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Order cancelled successfully.");
        } catch (OrderNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Order not found.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/user/orders";
    }

    //Payment failed
    @GetMapping("/user/processRazorpayPayment/failed")
    public ResponseEntity<String> processRazorpayPaymentFailed(@RequestParam("orderId") String orderId, Model model, HttpSession session) {
        Long couponId = (Long) session.getAttribute("coupon");
        if (couponId == null) {
            couponId = 0L;
        }
        CouponDto couponDto = couponService.getCouponById(couponId);
        Long couponAmount = null;
        if (couponDto != null) {
            couponAmount = (long) couponDto.getAmount();
        }
        String userEmail = userInfoService.currentUser();
        session.setAttribute("orderPlaced", "orderPlaced");
        Long selectedAddressId = (Long) session.getAttribute("selectedAddressId");
        String selectedPaymentMethod = (String) session.getAttribute("selectedPaymentMethod");
        session.removeAttribute("selectedAddressId");
        session.removeAttribute("selectedPaymentMethod");
        orderService.placeOrderByRazorpayFailed(orderId, selectedAddressId, selectedPaymentMethod, couponAmount);
        if (couponDto != null) {
            couponService.couponUsed(couponDto.getId(), userEmail);
            session.removeAttribute("coupon");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Payment Failed");
    }



    @GetMapping("/user/processRazorpayPayment")
    public ResponseEntity<String> processRazorpayPayment(@RequestParam("paymentId") String paymentId, Model model, HttpSession session) throws InsufficientStockException {
        Long couponId = (Long) session.getAttribute("coupon");
        if (couponId == null) {
            couponId = 0L;
        }
        CouponDto couponDto = couponService.getCouponById(couponId);
        Long couponAmount = null;
        if (couponDto != null) {
            couponAmount = (long) couponDto.getAmount();
        }
        String userEmail = userInfoService.currentUser();
        session.setAttribute("orderPlaced", "orderPlaced");
        Long selectedAddressId = (Long) session.getAttribute("selectedAddressId");
        String selectedPaymentMethod = (String) session.getAttribute("selectedPaymentMethod");
        session.removeAttribute("selectedAddressId");
        session.removeAttribute("selectedPaymentMethod");
        orderService.placeOrderByRazorpay(paymentId, selectedAddressId, selectedPaymentMethod, couponAmount);
        if (couponDto != null) {
            couponService.couponUsed(couponDto.getId(), userEmail);
            session.removeAttribute("coupon");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Payment processed successfully");
    }

    @PostMapping("/user/order/return")
    public String processOrderReturn(@RequestParam Long orderId,
                                     @RequestParam String returnReason,
                                     RedirectAttributes attributes) {

        try {

           Optional<ReturnRequest>  request = Optional.ofNullable(returnRequestService.getReturnRequestByOrderId(orderId));

           if (request != null){
               attributes.addFlashAttribute("error", "Return request already sended.");
               return "redirect:/user/orderDetails/"+orderId;
           }

            if ("Damaged".equals(returnReason)) {
                attributes.addFlashAttribute("error", "Return request cannot be processed for 'Damaged' items.");
                return "redirect:/user/orderDetails/"+orderId;
            }

            Orders order = orderService.getOrderById(orderId);

            if (LocalDate.now().isAfter(order.getReturnExpiryDate())){
                attributes.addFlashAttribute("error", "Return expiry date has been reached: " + order.getReturnExpiryDate());
                return "redirect:/user/orderDetails/"+orderId;
            }

            UserInfo user = userInfoService.currentUserDetail();

            ReturnRequest returnRequest = new ReturnRequest();
            returnRequest.setOrder(order);
            returnRequest.setUser(user);
            returnRequest.setRequestDate(LocalDate.now());
            returnRequest.setAccept(false);
            returnRequest.setReject(false);
            returnRequest.setReturnReason(returnReason);

            returnRequestService.saveReturnRequest(returnRequest);

            attributes.addFlashAttribute("success", "Return request sent successfully.");
        } catch (Exception e) {
            String errorMessage = "Error in return request: " + e.getMessage();
            System.err.println(errorMessage);
            attributes.addFlashAttribute("error", "Error in return request");
        }
        return "redirect:/user/orderDetails/"+orderId;
    }


    @GetMapping("/user/failed/order/payment/{id}")
    public String failedPaymentPay(@PathVariable("id") long orderId,Model model,HttpSession session) throws RazorpayException {
        String razorpayOrderId = orderService.generateRazorpayOrderForFailed(orderId);
        Orders order = orderService.getOrderById(orderId);
        session.setAttribute("orderId",orderId);
        String userEmail = userInfoService.currentUser();
        model.addAttribute("username",userEmail);
        model.addAttribute("email",userEmail);
        model.addAttribute("razorpayOrderId", razorpayOrderId);
        model.addAttribute("totalOrderAmount", order.getTotalAmount());
        return "user/FailedOrderPayment";
    }

    @GetMapping("/user/Failed/processRazorpayPayment")
    public ResponseEntity<String> processRazorpayPayment(@RequestParam("paymentId") String paymentId,HttpSession session) {
      session.setAttribute("orderPlaced","orderPlaced");
       long orderId = (long) session.getAttribute("orderId");
       orderService.successFailedOrder(orderId);
        return ResponseEntity.ok("success");
    }

    @GetMapping("/user/failed/order/payment/cancel/{id}")
    public String cancelFailedPayment(@PathVariable("id") Long orderId,RedirectAttributes attributes){
    boolean check = orderService.removeFailedOrder(orderId);
     if (check == true){
         attributes.addFlashAttribute("success","Order cancel successful.");
         return "redirect:/user/orders";
     }
     attributes.addFlashAttribute("error","Order cancel failed.");
        return "redirect:/user/orders";
    }
}
