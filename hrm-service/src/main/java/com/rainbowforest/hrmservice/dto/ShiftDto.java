package com.rainbowforest.hrmservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

public class ShiftDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotBlank
        private String name;
        @NotNull
        private LocalTime startTime;
        @NotNull
        private LocalTime endTime;
        private Long branchId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String name;
        private LocalTime startTime;
        private LocalTime endTime;
        private Long branchId;
        private String branchName;
    }
}
