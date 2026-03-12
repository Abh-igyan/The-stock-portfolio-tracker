package com.portfolio.api.controller;

import com.portfolio.api.dto.*;
import com.portfolio.api.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<PortfolioDTO.Response> create(@Valid @RequestBody PortfolioDTO.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.createPortfolio(req));
    }

    @GetMapping
    public ResponseEntity<List<PortfolioDTO.Response>> getAll(
            @RequestParam(required = false) String owner) {
        if (owner != null) {
            return ResponseEntity.ok(portfolioService.getPortfoliosByOwner(owner));
        }
        return ResponseEntity.ok(portfolioService.getAllPortfolios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioDTO.DetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.getPortfolioById(id));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<PortfolioStatsDTO> getStats(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.getStats(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortfolioDTO.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody PortfolioDTO.CreateRequest req) {
        return ResponseEntity.ok(portfolioService.updatePortfolio(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }
}
