package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.user.Wallet;
import com.camcorderio.camcorderio.entity.user.WalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {
    Wallet findByUserEmail(String email);

}