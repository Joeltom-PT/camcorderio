package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.user.Orders;
import com.camcorderio.camcorderio.entity.user.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Orders,Long> {
    List<Orders> findByUserEmail(String userEmail);

    Orders findByUserEmailAndOrderId(String userEmail, Long orderId);

    @Query("SELECT o FROM Orders o WHERE o.status <> 'Pending' ORDER BY o.orderId ASC")
    Page<Orders> findAllWithoutPendingStatus(Pageable pageable);

    List<Orders> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);

    Orders findByOrderIdAndUser_Email(Long orderId, String email);

    @Query("SELECT COUNT(o) FROM Orders o WHERE DATE(o.orderDate) = :date")
    Integer getTotalOrdersByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(o) FROM Orders o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Integer getTotalOrdersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
