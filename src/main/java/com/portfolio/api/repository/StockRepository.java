package com.portfolio.api.repository;

import com.portfolio.api.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByPortfolioId(Long portfolioId);

    Optional<Stock> findByPortfolioIdAndId(Long portfolioId, Long stockId);

    boolean existsByPortfolioIdAndTickerIgnoreCase(Long portfolioId, String ticker);

    @Query("SELECT s FROM Stock s WHERE s.portfolio.id = :portfolioId ORDER BY s.ticker ASC")
    List<Stock> findByPortfolioIdOrderByTicker(Long portfolioId);

    @Query("SELECT s FROM Stock s WHERE UPPER(s.ticker) = UPPER(:ticker)")
    List<Stock> findByTickerIgnoreCase(String ticker);
}
