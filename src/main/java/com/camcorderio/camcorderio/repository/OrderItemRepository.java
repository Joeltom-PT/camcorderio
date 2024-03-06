package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.user.OrderItem;
import com.camcorderio.camcorderio.model.admin.OrderItemSalesReportDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem , Long> {
    List<OrderItem> findByOrders_OrderId(Long orderId);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi " +
            "WHERE oi.orders.orderDate BETWEEN :startDate AND :endDate")
    Integer getTotalQuantityByMonth(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi " +
            "WHERE YEAR(oi.orders.orderDate) = :year")
    Integer getTotalQuantityByYear(@Param("year") int year);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi " +
            "WHERE oi.orders.orderDate BETWEEN :startDate AND :endDate")
    Integer getTotalQuantityByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(oi.quantity * p.price) FROM OrderItem oi JOIN oi.product p WHERE DATE(oi.orders.orderDate) = :date")
    Integer getTotalAmountByDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE DATE(oi.orders.orderDate) = :date")
    Integer getTotalQuantityByDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(oi.quantity * p.price) FROM OrderItem oi JOIN oi.product p WHERE oi.orders.orderDate BETWEEN :startDate AND :endDate")
    Integer getTotalAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT oi.product, SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "GROUP BY oi.product " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTop10SellingProducts();

    @Query("SELECT p.category, SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "GROUP BY p.category " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTop10SellingCategories();

}
