package com.portfolio.api.service;

import com.portfolio.api.dto.StockDTO;
import com.portfolio.api.exception.ResourceNotFoundException;
import com.portfolio.api.model.*;
import com.portfolio.api.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

    private final StockRepository stockRepository;
    private final PortfolioService portfolioService;

    public StockDTO.Response addStock(Long portfolioId, StockDTO.CreateRequest req) {
        Portfolio portfolio = portfolioService.findPortfolio(portfolioId);

        if (stockRepository.existsByPortfolioIdAndTickerIgnoreCase(portfolioId, req.getTicker())) {
            throw new IllegalArgumentException(
                    "Ticker '" + req.getTicker().toUpperCase() + "' already exists in this portfolio. " +
                    "Update the existing position instead.");
        }

        Stock stock = Stock.builder()
                .ticker(req.getTicker().toUpperCase())
                .companyName(req.getCompanyName())
                .quantity(req.getQuantity())
                .buyPrice(req.getBuyPrice())
                .currentPrice(req.getCurrentPrice())
                .portfolio(portfolio)
                .build();

        return toResponse(stockRepository.save(stock));
    }

    @Transactional(readOnly = true)
    public List<StockDTO.Response> getStocksForPortfolio(Long portfolioId) {
        portfolioService.findPortfolio(portfolioId); // validate exists
        return stockRepository.findByPortfolioIdOrderByTicker(portfolioId).stream()
                .map(StockService::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StockDTO.Response getStock(Long portfolioId, Long stockId) {
        return toResponse(findStock(portfolioId, stockId));
    }

    public StockDTO.Response updateStock(Long portfolioId, Long stockId, StockDTO.UpdateRequest req) {
        Stock stock = findStock(portfolioId, stockId);
        if (req.getQuantity() != null) stock.setQuantity(req.getQuantity());
        if (req.getBuyPrice() != null) stock.setBuyPrice(req.getBuyPrice());
        if (req.getCurrentPrice() != null) stock.setCurrentPrice(req.getCurrentPrice());
        return toResponse(stockRepository.save(stock));
    }

    public StockDTO.Response updatePrice(Long portfolioId, Long stockId, StockDTO.PriceUpdateRequest req) {
        Stock stock = findStock(portfolioId, stockId);
        stock.setCurrentPrice(req.getCurrentPrice());
        return toResponse(stockRepository.save(stock));
    }

    public void deleteStock(Long portfolioId, Long stockId) {
        Stock stock = findStock(portfolioId, stockId);
        stockRepository.delete(stock);
    }

    // ── HELPERS ─────────────────────────────────────────────────────────

    private Stock findStock(Long portfolioId, Long stockId) {
        return stockRepository.findByPortfolioIdAndId(portfolioId, stockId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found with id " + stockId + " in portfolio " + portfolioId));
    }

    public static StockDTO.Response toResponse(Stock s) {
        return StockDTO.Response.builder()
                .id(s.getId())
                .ticker(s.getTicker())
                .companyName(s.getCompanyName())
                .quantity(s.getQuantity())
                .buyPrice(s.getBuyPrice())
                .currentPrice(s.getCurrentPrice())
                .costBasis(s.getCostBasis())
                .marketValue(s.getMarketValue())
                .pnl(s.getPnL())
                .pnlPercent(s.getPnLPercent())
                .portfolioId(s.getPortfolio().getId())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
