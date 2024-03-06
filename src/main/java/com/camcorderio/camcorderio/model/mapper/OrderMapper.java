package com.camcorderio.camcorderio.model.mapper;

import com.camcorderio.camcorderio.model.admin.AdminAddressDto;
import com.camcorderio.camcorderio.model.admin.AdminOrderDetailDto;
import com.camcorderio.camcorderio.entity.user.Orders;

public class OrderMapper {

    public static AdminOrderDetailDto mapToAdminOrderDetailDto(Orders order) {
        AdminOrderDetailDto adminOrderDetailDto = new AdminOrderDetailDto();
        adminOrderDetailDto.setOrderId(order.getOrderId());
        adminOrderDetailDto.setUserId(order.getUser().getUserId());
        adminOrderDetailDto.setUsername(order.getUser().getUsername());
        adminOrderDetailDto.setEmail(order.getUser().getEmail());
        adminOrderDetailDto.setStatus(order.getStatus());
        adminOrderDetailDto.setPhoneNumber(order.getUser().getPhoneNumber());
        adminOrderDetailDto.setAddress(mapToAdminAddressDto(order.getAddress()));
        adminOrderDetailDto.setOrderDate(order.getOrderDate());
        adminOrderDetailDto.setTotalAmount(order.getTotalAmount());
        adminOrderDetailDto.setPaymentStatus(order.getPayments() != null ? order.getPayments().getStatus() : null);
        adminOrderDetailDto.setDeliveryStatus(order.getStatus());
        adminOrderDetailDto.setDeliveredDate(order.getDeliveredDate());
        adminOrderDetailDto.setReturnExpiryDate(order.getReturnExpiryDate());
        adminOrderDetailDto.setCancelled(order.isCancelled());
        adminOrderDetailDto.setRefundStatus(order.isRefundStatus());

        return adminOrderDetailDto;
    }

    private static AdminAddressDto mapToAdminAddressDto(com.camcorderio.camcorderio.entity.user.Address address) {
        if (address == null) {
            return null;
        }

        AdminAddressDto adminAddressDto = new AdminAddressDto();
        adminAddressDto.setId(address.getId());
        adminAddressDto.setName(address.getName());
        adminAddressDto.setMobile(address.getMobile());
        adminAddressDto.setAddress(address.getAddress());
        adminAddressDto.setCity(address.getCity());
        adminAddressDto.setPin(address.getPin());
        adminAddressDto.setState(address.getState());

        return adminAddressDto;
    }
}
