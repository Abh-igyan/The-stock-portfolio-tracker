# 📈 Stock Portfolio Tracker API

A production-ready REST API for tracking stock portfolios, P&L, and portfolio analytics — built with Java 17, Spring Boot 3, and MySQL.

---

## Tech Stack

| Layer       | Technology                        |
|-------------|-----------------------------------|
| Language    | Java 17                           |
| Framework   | Spring Boot 3.2                   |
| Database    | MySQL (prod) / H2 (tests)         |
| ORM         | Spring Data JPA / Hibernate       |
| Validation  | Jakarta Bean Validation           |
| CI/CD       | GitHub Actions                    |
| Deployment  | Railway.app / Render              |

---

## API Endpoints

### Portfolios

| Method | Endpoint                        | Description                          |
|--------|---------------------------------|--------------------------------------|
| POST   | `/api/v1/portfolios`            | Create a portfolio                   |
| GET    | `/api/v1/portfolios`            | List all portfolios                  |
| GET    | `/api/v1/portfolios?owner=name` | Filter by owner                      |
| GET    | `/api/v1/portfolios/{id}`       | Get portfolio + all holdings         |
| GET    | `/api/v1/portfolios/{id}/stats` | P&L stats + allocation breakdown     |
| PUT    | `/api/v1/portfolios/{id}`       | Update portfolio metadata            |
| DELETE | `/api/v1/portfolios/{id}`       | Delete portfolio (cascades to stocks)|

### Stocks

| Method | Endpoint                                         | Description                    |
|--------|--------------------------------------------------|--------------------------------|
| POST   | `/api/v1/portfolios/{pid}/stocks`                | Add a stock position           |
| GET    | `/api/v1/portfolios/{pid}/stocks`                | List all stocks in portfolio   |
| GET    | `/api/v1/portfolios/{pid}/stocks/{sid}`          | Get single stock               |
| PUT    | `/api/v1/portfolios/{pid}/stocks/{sid}`          | Update quantity/prices         |
| PATCH  | `/api/v1/portfolios/{pid}/stocks/{sid}/price`    | Update current price only      |
| DELETE | `/api/v1/portfolios/{pid}/stocks/{sid}`          | Remove stock from portfolio    |

---

## Sample Requests

### Create a portfolio
```bash
curl -X POST http://localhost:8080/api/v1/portfolios \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tech Portfolio",
    "description": "My FAANG holdings",
    "ownerName": "Alice"
  }'
```

### Add a stock
```bash
curl -X POST http://localhost:8080/api/v1/portfolios/1/stocks \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "companyName": "Apple Inc.",
    "quantity": 10,
    "buyPrice": 150.00,
    "currentPrice": 182.50
  }'
```

### Update current price
```bash
curl -X PATCH http://localhost:8080/api/v1/portfolios/1/stocks/1/price \
  -H "Content-Type: application/json" \
  -d '{"currentPrice": 195.00}'
```

### Get portfolio stats
```bash
curl http://localhost:8080/api/v1/portfolios/1/stats
```

Response:
```json
{
  "portfolioId": 1,
  "portfolioName": "Tech Portfolio",
  "totalPositions": 3,
  "totalCostBasis": 4500.00,
  "totalMarketValue": 5250.00,
  "totalPnL": 750.00,
  "totalPnLPercent": 16.67,
  "bestPerformer": "NVDA",
  "bestPerformerPnLPercent": 38.50,
  "worstPerformer": "INTC",
  "worstPerformerPnLPercent": -12.30,
  "allocations": [
    {
      "ticker": "AAPL",
      "companyName": "Apple Inc.",
      "marketValue": 1950.00,
      "allocationPercent": 37.14,
      "pnl": 200.00,
      "pnlPercent": 11.43
    }
  ]
}
```

---

## Local Development

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+ running locally

### Setup

```bash
# 1. Clone the repo
git clone https://github.com/YOUR_USERNAME/stock-portfolio-api.git
cd stock-portfolio-api

# 2. Create local MySQL database
mysql -u root -p -e "CREATE DATABASE portfolio_db;"

# 3. Set environment variables (or edit application.properties)
export MYSQL_URL=jdbc:mysql://localhost:3306/portfolio_db?createDatabaseIfNotExist=true
export MYSQL_USER=root
export MYSQL_PASSWORD=yourpassword

# 4. Run
mvn spring-boot:run
```

The API will be live at `http://localhost:8080`.

### Run Tests

```bash
mvn test
```

Tests use H2 in-memory database — no MySQL needed.

---

## Deploy to Railway

1. Push this repo to GitHub
2. Go to [railway.app](https://railway.app) → **New Project → Deploy from GitHub Repo**
3. Add a **MySQL** plugin inside the project
4. Railway auto-injects `MYSQL_URL`, `MYSQL_USER`, `MYSQL_PASSWORD`
5. Set `PORT=8080` in Railway environment variables if not auto-detected
6. Deploy ✅

### Deploy to Render

1. Push to GitHub
2. New Web Service → connect your repo
3. **Build Command:** `mvn -B package -DskipTests`
4. **Start Command:** `java -jar target/*.jar`
5. Add a free **MySQL** database and copy the connection string
6. Set environment variables: `MYSQL_URL`, `MYSQL_USER`, `MYSQL_PASSWORD`

---

## CI/CD Pipeline

GitHub Actions runs automatically on every push to `main` or `develop`:

1. Compile the project
2. Run all integration tests (H2 in-memory)
3. Build a production JAR
4. Upload test reports + JAR as artifacts

---

## Project Structure

```
src/
├── main/java/com/portfolio/api/
│   ├── controller/        # REST endpoints
│   ├── service/           # Business logic + P&L calculations
│   ├── repository/        # Spring Data JPA interfaces
│   ├── model/             # JPA entities (Portfolio, Stock)
│   ├── dto/               # Request/Response objects
│   └── exception/         # Custom exceptions + global handler
└── test/java/com/portfolio/api/
    └── PortfolioApiIntegrationTest.java
```

---

## Key Design Decisions

- **Derived calculations** (P&L, market value) computed at query time — no stale data stored
- **Cascade delete** on Portfolio removes all associated stocks automatically
- **Duplicate ticker protection** per portfolio — prevents double-entries
- **`currentPrice` is optional** — you can add positions without a live price and update later
- **H2 for tests** — zero DB setup needed to run the test suite
