package com.rainbowforest.hrmservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "branches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "address", length = 255)
    private String address;

    // Tọa độ GPS của chi nhánh
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // Bán kính cho phép chấm công (mét)
    @Column(name = "radius_meters")
    private Double radiusMeters;

    // BSSID (MAC address) của WiFi chi nhánh – dùng ở production
    @Column(name = "bssid", length = 50)
    private String bssid;

    // SSID (tên mạng) của WiFi chi nhánh – dùng ở demo
    @Column(name = "ssid", length = 100)
    private String ssid;

    // true = chế độ demo (chỉ kiểm tra SSID), false = production (kiểm tra BSSID)
    @Column(name = "is_demo", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isDemo = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
