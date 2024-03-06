package com.camcorderio.camcorderio.entity.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Entity
@Getter
@Setter
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_sequence")
    @SequenceGenerator(name = "payment_sequence", sequenceName = "payment_sequence", allocationSize = 1)
    @Column(name = "paymentId")
    private Integer paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentMethod")
    @Check(constraints = "payment_method IN ('RAZORPAY', 'COD','WALLET')")
    private PaymentMethod paymentMethod;

    @OneToOne
    private Orders orders;

    @Column(name = "status")
    private String status;

    @Column(name = "paymentTime")
    private String paymentTime;

    @Column(name = "amount")
    private double amount;

}
