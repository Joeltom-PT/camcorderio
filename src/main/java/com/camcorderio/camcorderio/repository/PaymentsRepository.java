package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.user.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentsRepository extends JpaRepository<Payments,Integer> {
}
