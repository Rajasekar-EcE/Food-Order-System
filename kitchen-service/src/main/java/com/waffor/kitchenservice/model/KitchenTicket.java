package com.waffor.kitchenservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "kitchen_tickets")
@Getter
@Setter
@NoArgsConstructor
public class KitchenTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String items;

    @Column(nullable = false)
    private String status; // PREPARING | READY

    @Column(name = "prep_time_seconds")
    private Integer prepTimeSeconds;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
