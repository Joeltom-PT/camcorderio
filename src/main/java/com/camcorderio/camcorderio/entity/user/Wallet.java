package com.camcorderio.camcorderio.entity.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wallet_sequence")
    @SequenceGenerator(name = "wallet_sequence", sequenceName = "wallet_sequence", allocationSize = 1)
    @Column(name = "walletId")
    private Integer walletId;

    @OneToOne
    private UserInfo user;

    private double totalAmount;

    @OneToMany(mappedBy = "wallet", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<WalletHistory> walletHistories;
}
