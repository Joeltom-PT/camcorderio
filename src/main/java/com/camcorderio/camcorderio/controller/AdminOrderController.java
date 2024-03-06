package com.camcorderio.camcorderio.controller;

import com.camcorderio.camcorderio.entity.user.Orders;
import com.camcorderio.camcorderio.model.admin.AdminOrderDetailDto;
import com.camcorderio.camcorderio.model.user.OrderDetailDto;
import com.camcorderio.camcorderio.model.user.OrderItemDto;
import com.camcorderio.camcorderio.repository.OrderRepository;
import com.camcorderio.camcorderio.service.user.OrderService;
import org.hibernate.query.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/admin/orders")
    public String ordersList(@PageableDefault(size = 10)Pageable pageable, Model model){
        Page<Orders> orderPage = orderService.getAllOrders(pageable);
        model.addAttribute("orders",orderPage);
        return "admin/orders/list";
    }

    @GetMapping("/admin/orders/update/{id}")
    public String updateOrder(@PathVariable("id") long orderId, Model model) {
        Optional<AdminOrderDetailDto> order = orderService.getOrder(orderId);

        AdminOrderDetailDto orderDetail = order.orElse(null);

        List<OrderItemDto> orderItem = orderService.orderItemsByOrderId(orderId);

        model.addAttribute("orderDetail", orderDetail);
        model.addAttribute("orderItem",orderItem);
        return "admin/orders/update";
    }

    @PostMapping("/admin/update/order")
    public String updateOrderDetails(@ModelAttribute AdminOrderDetailDto orderDetailDto) {
        orderService.updateOrderDetails(orderDetailDto);
        return "redirect:/admin/orders";
    }
}