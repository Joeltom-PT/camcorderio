package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.user.WalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletHistoryRepository extends JpaRepository<WalletHistory, Integer> {
}
