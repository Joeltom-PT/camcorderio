package com.camcorderio.camcorderio.service.user;

import com.camcorderio.camcorderio.entity.user.Wallet;

public interface WalletService {
    Wallet getWalletByEmail(String email);
}
