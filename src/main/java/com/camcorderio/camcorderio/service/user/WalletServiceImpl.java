package com.camcorderio.camcorderio.service.user;

import com.camcorderio.camcorderio.entity.user.Wallet;
import com.camcorderio.camcorderio.entity.user.WalletHistory;
import com.camcorderio.camcorderio.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WalletServiceImpl implements WalletService{

    private final WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public Wallet getWalletByEmail(String email) {
        Wallet wallet = walletRepository.findByUserEmail(email);
        return wallet;
    }
}
