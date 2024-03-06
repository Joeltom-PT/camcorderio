package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.user.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    @Query("SELECT rr FROM ReturnRequest rr WHERE rr.order.orderId = :orderId")
    ReturnRequest findByOrderId(@Param("orderId") Long orderId);
}
