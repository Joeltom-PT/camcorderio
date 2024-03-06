package com.camcorderio.camcorderio.entity.user;

public enum PaymentMethod {
    COD("Cash On Delivery"),

   RAZORPAY("Razorpay"),

    WALLET("Wallet");


    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
