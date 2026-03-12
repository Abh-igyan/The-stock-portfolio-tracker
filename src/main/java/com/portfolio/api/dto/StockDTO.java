package com.portfolio.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Ticker symbol is required")
        @Size(min = 1, max = 10)
        private String ticker;

        @NotBlank(message = "Company name is required")
        private String companyName;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        @NotNull(message = "Buy price is required")
        @DecimalMin(value = "0.01")
        private BigDecimal buyPrice;

        @DecimalMin(value = "0.01")
        private BigDecimal currentPrice;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateRequest {
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        @DecimalMin(value = "0.01")
        private BigDecimal buyPrice;

        @DecimalMin(value = "0.01")
        private BigDecimal currentPrice;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PriceUpdateRequest {
        @NotNull(message = "Current price is required")
        @DecimalMin(value = "0.01")
        private BigDecimal currentPrice;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private String ticker;
        private String companyName;
        private Integer quantity;
        private BigDecimal buyPrice;
        private BigDecimal currentPrice;
        private BigDecimal costBasis;
        private BigDecimal marketValue;
        private BigDecimal pnl;
        private BigDecimal pnlPercent;
        private Long portfolioId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
