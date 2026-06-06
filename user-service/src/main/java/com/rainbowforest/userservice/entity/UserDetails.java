package com.rainbowforest.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "users_details")
public class UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "street", length = 30)
    private String street;

    @Column(name = "street_number", length = 10)
    private String streetNumber;

    @Column(name = "zip_code", length = 6)
    private String zipCode;

    @Column(name = "locality", length = 30)
    private String locality;

    @Column(name = "country", length = 30)
    private String country;

    // ── Thông tin nhân viên ──────────────────────────────────────────────────
    /** Phòng/ban (vd: "Kế toán", "Bếp", "Phục vụ") */
    @Column(name = "department", length = 100)
    private String department;

    /** Chức vụ (vd: "Trưởng ca", "Nhân viên", "Quản lý") */
    @Column(name = "position", length = 100)
    private String position;

    /** Ngày vào làm */
    @Column(name = "hire_date")
    private LocalDate hireDate;
    // ────────────────────────────────────────────────────────────────────────

    @OneToOne(mappedBy = "userDetails")
    @JsonIgnore
    private User user;

    public UserDetails() {
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}