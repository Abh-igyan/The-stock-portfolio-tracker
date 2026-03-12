package com.portfolio.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.api.dto.PortfolioDTO;
import com.portfolio.api.dto.StockDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PortfolioApiIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    private static Long portfolioId;
    private static Long stockId;

    // ── PORTFOLIO TESTS ──────────────────────────────────────────────────────

    @Test @Order(1)
    void createPortfolio_success() throws Exception {
        var req = PortfolioDTO.CreateRequest.builder()
                .name("Tech Portfolio")
                .description("My tech holdings")
                .ownerName("Alice")
                .build();

        MvcResult result = mvc.perform(post("/api/v1/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Tech Portfolio"))
                .andExpect(jsonPath("$.ownerName").value("Alice"))
                .andReturn();

        portfolioId = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test @Order(2)
    void createPortfolio_missingName_returnsBadRequest() throws Exception {
        var req = PortfolioDTO.CreateRequest.builder()
                .ownerName("Bob")
                .build();

        mvc.perform(post("/api/v1/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test @Order(3)
    void getAllPortfolios_returnsNonEmptyList() throws Exception {
        mvc.perform(get("/api/v1/portfolios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test @Order(4)
    void getPortfolioById_success() throws Exception {
        mvc.perform(get("/api/v1/portfolios/" + portfolioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(portfolioId));
    }

    @Test @Order(5)
    void getPortfolioById_notFound_returns404() throws Exception {
        mvc.perform(get("/api/v1/portfolios/99999"))
                .andExpect(status().isNotFound());
    }

    // ── STOCK TESTS ──────────────────────────────────────────────────────────

    @Test @Order(6)
    void addStock_success() throws Exception {
        var req = StockDTO.CreateRequest.builder()
                .ticker("AAPL")
                .companyName("Apple Inc.")
                .quantity(10)
                .buyPrice(new BigDecimal("150.00"))
                .currentPrice(new BigDecimal("175.00"))
                .build();

        MvcResult result = mvc.perform(post("/api/v1/portfolios/" + portfolioId + "/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("AAPL"))
                .andExpect(jsonPath("$.costBasis").value(1500.00))
                .andExpect(jsonPath("$.marketValue").value(1750.00))
                .andExpect(jsonPath("$.pnl").value(250.00))
                .andReturn();

        stockId = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test @Order(7)
    void addDuplicateTicker_returnsBadRequest() throws Exception {
        var req = StockDTO.CreateRequest.builder()
                .ticker("AAPL")
                .companyName("Apple Inc.")
                .quantity(5)
                .buyPrice(new BigDecimal("160.00"))
                .build();

        mvc.perform(post("/api/v1/portfolios/" + portfolioId + "/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(8)
    void updateStockPrice_recalculatesPnL() throws Exception {
        var req = new StockDTO.PriceUpdateRequest(new BigDecimal("200.00"));

        mvc.perform(patch("/api/v1/portfolios/" + portfolioId + "/stocks/" + stockId + "/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPrice").value(200.00))
                .andExpect(jsonPath("$.pnl").value(500.00));
    }

    @Test @Order(9)
    void getPortfolioStats_returnsCorrectData() throws Exception {
        mvc.perform(get("/api/v1/portfolios/" + portfolioId + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPositions").value(1))
                .andExpect(jsonPath("$.bestPerformer").value("AAPL"))
                .andExpect(jsonPath("$.allocations", hasSize(1)));
    }

    @Test @Order(10)
    void deleteStock_success() throws Exception {
        mvc.perform(delete("/api/v1/portfolios/" + portfolioId + "/stocks/" + stockId))
                .andExpect(status().isNoContent());
    }

    @Test @Order(11)
    void deletePortfolio_success() throws Exception {
        mvc.perform(delete("/api/v1/portfolios/" + portfolioId))
                .andExpect(status().isNoContent());
    }
}
