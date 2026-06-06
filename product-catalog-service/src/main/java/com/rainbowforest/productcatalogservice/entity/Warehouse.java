package com.rainbowforest.productcatalogservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Kho hàng / địa điểm lưu trữ nguyên liệu & hàng hóa.
 */
@Entity
@Table(name = "warehouses")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    @NotNull
    private String name;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "note", length = 500)
    private String note;

    public Warehouse() {}

    // ── Getters & Setters ────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
