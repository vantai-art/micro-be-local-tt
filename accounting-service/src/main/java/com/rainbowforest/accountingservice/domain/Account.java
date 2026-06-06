package com.rainbowforest.accountingservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    // asset | liability | equity | revenue | expense
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "balance", precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "description", length = 255)
    private String description;
}
