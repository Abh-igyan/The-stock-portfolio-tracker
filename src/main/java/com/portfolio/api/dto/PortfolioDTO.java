package com.portfolio.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// ──────────────────────────────────────────
// PORTFOLIO DTOs
// ──────────────────────────────────────────

public class PortfolioDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Portfolio name is required")
        private String name;

        @Size(max = 500)
        private String description;

        @NotBlank(message = "Owner name is required")
        private String ownerName;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String ownerName;
        private int totalPositions;
        private BigDecimal totalCostBasis;
        private BigDecimal totalMarketValue;
        private BigDecimal totalPnL;
        private BigDecimal totalPnLPercent;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetailResponse {
        private Long id;
        private String name;
        private String description;
        private String ownerName;
        private List<StockDTO.Response> stocks;
        private BigDecimal totalCostBasis;
        private BigDecimal totalMarketValue;
        private BigDecimal totalPnL;
        private BigDecimal totalPnLPercent;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
