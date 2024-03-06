package com.camcorderio.camcorderio.entity.user;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "returnRequest_sequence")
    @SequenceGenerator(name = "returnRequest_sequence", sequenceName = "returnRequest_sequence", allocationSize = 1)
    @Column(name = "return_request_id")
    private Long returnRequestId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Orders order;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo user;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "return_reason")
    private String returnReason;

    @Column(name = "accept")
    private boolean accept;

    @Column(name = "reject")
    private boolean reject;

}
