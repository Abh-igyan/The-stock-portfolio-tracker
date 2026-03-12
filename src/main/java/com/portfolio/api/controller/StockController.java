package com.portfolio.api.controller;

import com.portfolio.api.dto.StockDTO;
import com.portfolio.api.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolios/{portfolioId}/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping
    public ResponseEntity<StockDTO.Response> addStock(
            @PathVariable Long portfolioId,
            @Valid @RequestBody StockDTO.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockService.addStock(portfolioId, req));
    }

    @GetMapping
    public ResponseEntity<List<StockDTO.Response>> getAll(@PathVariable Long portfolioId) {
        return ResponseEntity.ok(stockService.getStocksForPortfolio(portfolioId));
    }

    @GetMapping("/{stockId}")
    public ResponseEntity<StockDTO.Response> getOne(
            @PathVariable Long portfolioId,
            @PathVariable Long stockId) {
        return ResponseEntity.ok(stockService.getStock(portfolioId, stockId));
    }

    @PutMapping("/{stockId}")
    public ResponseEntity<StockDTO.Response> update(
            @PathVariable Long portfolioId,
            @PathVariable Long stockId,
            @Valid @RequestBody StockDTO.UpdateRequest req) {
        return ResponseEntity.ok(stockService.updateStock(portfolioId, stockId, req));
    }

    @PatchMapping("/{stockId}/price")
    public ResponseEntity<StockDTO.Response> updatePrice(
            @PathVariable Long portfolioId,
            @PathVariable Long stockId,
            @Valid @RequestBody StockDTO.PriceUpdateRequest req) {
        return ResponseEntity.ok(stockService.updatePrice(portfolioId, stockId, req));
    }

    @DeleteMapping("/{stockId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long portfolioId,
            @PathVariable Long stockId) {
        stockService.deleteStock(portfolioId, stockId);
        return ResponseEntity.noContent().build();
    }
}
