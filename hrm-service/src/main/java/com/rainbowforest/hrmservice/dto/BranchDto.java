package com.rainbowforest.hrmservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

public class BranchDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotBlank
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
        private Double radiusMeters;
        private String bssid;
        private String ssid;
        private Boolean isDemo;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
        private Double radiusMeters;
        private String bssid;
        private String ssid;
        private Boolean isDemo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
