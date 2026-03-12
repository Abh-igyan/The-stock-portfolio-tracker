package com.portfolio.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ticker symbol is required")
    @Size(min = 1, max = 10, message = "Ticker must be 1–10 characters")
    @Column(nullable = false, length = 10)
    private String ticker;

    @NotBlank(message = "Company name is required")
    @Column(nullable = false)
    private String companyName;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Buy price is required")
    @DecimalMin(value = "0.01", message = "Buy price must be positive")
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal buyPrice;

    @DecimalMin(value = "0.01", message = "Current price must be positive")
    @Column(precision = 15, scale = 4)
    private BigDecimal currentPrice;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Derived calculations
    public BigDecimal getCostBasis() {
        return buyPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getMarketValue() {
        if (currentPrice == null) return null;
        return currentPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getPnL() {
        if (currentPrice == null) return null;
        return getMarketValue().subtract(getCostBasis());
    }

    public BigDecimal getPnLPercent() {
        if (currentPrice == null) return null;
        return getPnL().divide(getCostBasis(), 4, java.math.RoundingMode.HALF_UP)
                       .multiply(BigDecimal.valueOf(100));
    }
}
