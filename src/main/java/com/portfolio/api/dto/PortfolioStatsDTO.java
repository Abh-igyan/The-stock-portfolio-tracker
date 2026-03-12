package com.portfolio.api.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PortfolioStatsDTO {
    private Long portfolioId;
    private String portfolioName;
    private int totalPositions;
    private BigDecimal totalCostBasis;
    private BigDecimal totalMarketValue;
    private BigDecimal totalPnL;
    private BigDecimal totalPnLPercent;
    private String bestPerformer;
    private BigDecimal bestPerformerPnLPercent;
    private String worstPerformer;
    private BigDecimal worstPerformerPnLPercent;
    private List<AllocationDTO> allocations;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AllocationDTO {
        private String ticker;
        private String companyName;
        private BigDecimal marketValue;
        private BigDecimal allocationPercent;
        private BigDecimal pnl;
        private BigDecimal pnlPercent;
    }
}
