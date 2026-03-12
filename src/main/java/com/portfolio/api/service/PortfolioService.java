package com.portfolio.api.service;

import com.portfolio.api.dto.*;
import com.portfolio.api.exception.ResourceNotFoundException;
import com.portfolio.api.model.*;
import com.portfolio.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;

    // ── PORTFOLIO CRUD ──────────────────────────────────────────────────

    public PortfolioDTO.Response createPortfolio(PortfolioDTO.CreateRequest req) {
        Portfolio portfolio = Portfolio.builder()
                .name(req.getName())
                .description(req.getDescription())
                .ownerName(req.getOwnerName())
                .build();
        return toResponse(portfolioRepository.save(portfolio));
    }

    @Transactional(readOnly = true)
    public List<PortfolioDTO.Response> getAllPortfolios() {
        return portfolioRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PortfolioDTO.DetailResponse getPortfolioById(Long id) {
        Portfolio p = findPortfolio(id);
        return toDetailResponse(p);
    }

    @Transactional(readOnly = true)
    public List<PortfolioDTO.Response> getPortfoliosByOwner(String ownerName) {
        return portfolioRepository.findByOwnerNameIgnoreCase(ownerName).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PortfolioDTO.Response updatePortfolio(Long id, PortfolioDTO.CreateRequest req) {
        Portfolio p = findPortfolio(id);
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setOwnerName(req.getOwnerName());
        return toResponse(portfolioRepository.save(p));
    }

    public void deletePortfolio(Long id) {
        if (!portfolioRepository.existsById(id)) throw new ResourceNotFoundException("Portfolio", id);
        portfolioRepository.deleteById(id);
    }

    // ── STATS ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PortfolioStatsDTO getStats(Long portfolioId) {
        Portfolio p = findPortfolio(portfolioId);
        List<Stock> stocks = stockRepository.findByPortfolioIdOrderByTicker(portfolioId);

        List<Stock> priced = stocks.stream().filter(s -> s.getCurrentPrice() != null).toList();

        BigDecimal totalCost = stocks.stream()
                .map(Stock::getCostBasis)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMV = priced.stream()
                .map(Stock::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPnL = priced.stream()
                .map(Stock::getPnL)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPnLPct = totalCost.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : totalPnL.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        // Best / worst performer
        Optional<Stock> best = priced.stream()
                .max(Comparator.comparing(Stock::getPnLPercent));
        Optional<Stock> worst = priced.stream()
                .min(Comparator.comparing(Stock::getPnLPercent));

        // Allocations
        List<PortfolioStatsDTO.AllocationDTO> allocations = priced.stream()
                .map(s -> {
                    BigDecimal allocPct = totalMV.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                            : s.getMarketValue().divide(totalMV, 4, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(100));
                    return PortfolioStatsDTO.AllocationDTO.builder()
                            .ticker(s.getTicker())
                            .companyName(s.getCompanyName())
                            .marketValue(s.getMarketValue())
                            .allocationPercent(allocPct)
                            .pnl(s.getPnL())
                            .pnlPercent(s.getPnLPercent())
                            .build();
                })
                .sorted(Comparator.comparing(PortfolioStatsDTO.AllocationDTO::getAllocationPercent).reversed())
                .collect(Collectors.toList());

        return PortfolioStatsDTO.builder()
                .portfolioId(p.getId())
                .portfolioName(p.getName())
                .totalPositions(stocks.size())
                .totalCostBasis(totalCost)
                .totalMarketValue(totalMV)
                .totalPnL(totalPnL)
                .totalPnLPercent(totalPnLPct)
                .bestPerformer(best.map(Stock::getTicker).orElse(null))
                .bestPerformerPnLPercent(best.map(Stock::getPnLPercent).orElse(null))
                .worstPerformer(worst.map(Stock::getTicker).orElse(null))
                .worstPerformerPnLPercent(worst.map(Stock::getPnLPercent).orElse(null))
                .allocations(allocations)
                .build();
    }

    // ── HELPERS ─────────────────────────────────────────────────────────

    public Portfolio findPortfolio(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", id));
    }

    private PortfolioDTO.Response toResponse(Portfolio p) {
        List<Stock> stocks = p.getStocks();

        BigDecimal totalCost = stocks.stream()
                .map(Stock::getCostBasis)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Stock> priced = stocks.stream().filter(s -> s.getCurrentPrice() != null).toList();

        BigDecimal totalMV = priced.stream()
                .map(Stock::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPnL = priced.stream()
                .map(Stock::getPnL)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPnLPct = totalCost.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : totalPnL.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return PortfolioDTO.Response.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .ownerName(p.getOwnerName())
                .totalPositions(stocks.size())
                .totalCostBasis(totalCost)
                .totalMarketValue(totalMV)
                .totalPnL(totalPnL)
                .totalPnLPercent(totalPnLPct)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private PortfolioDTO.DetailResponse toDetailResponse(Portfolio p) {
        List<StockDTO.Response> stockDTOs = p.getStocks().stream()
                .map(StockService::toResponse)
                .collect(Collectors.toList());

        PortfolioDTO.Response summary = toResponse(p);
        return PortfolioDTO.DetailResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .ownerName(p.getOwnerName())
                .stocks(stockDTOs)
                .totalCostBasis(summary.getTotalCostBasis())
                .totalMarketValue(summary.getTotalMarketValue())
                .totalPnL(summary.getTotalPnL())
                .totalPnLPercent(summary.getTotalPnLPercent())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
