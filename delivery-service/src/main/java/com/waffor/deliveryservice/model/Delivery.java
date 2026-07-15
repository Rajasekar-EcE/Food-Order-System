package com.waffor.deliveryservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "driver_name")
    private String driverName;

    @Column(nullable = false)
    private String status; // ASSIGNED | DELIVERED

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
