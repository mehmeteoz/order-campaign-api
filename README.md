# Order & Campaign Service

A REST API built with Spring Boot 3 for managing shopping orders and dynamically evaluating campaign discounts.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.x |
| Database | PostgreSQL 15 |
| Cache | Spring Cache (In-Memory) |
| ORM | Spring Data JPA / Hibernate 6 |
| Container | Docker + Docker Compose |

## Getting Started

```bash
docker-compose up -d --build
```

This single command:
- Builds the Java application via multi-stage Docker build
- Starts the PostgreSQL database and the API container
- Auto-generates database schemas via Hibernate DDL
- Seeds the database with default products, categories, authors, and campaigns.

**API base URL:** `http://localhost:8080/api/v1/orders`

## Authentication

All endpoints require an `x-api-key` header to prevent unauthorized access.

| Token | Description |
|-------|------------|
| `VerySecretApiKey123` | Default API key configured in application.properties |

```http
x-api-key: VerySecretApiKey123
```

## API Endpoints

### Orders

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/orders` | Create an order with items, auto-applying the best campaign discount |
| `GET` | `/api/v1/orders/{id}` | Get detailed order information including financial breakdown |

## Campaign Engine

The campaign engine evaluates all active campaigns cached in memory and applies the one providing the **highest discount**. Campaigns are implemented via the Strategy Design Pattern (`CampaignStrategy`) — adding a new campaign requires no changes to the engine's core logic.

### Active Campaigns (Configured via JSON)

| Campaign | Type Enum | Description |
|----------|-----------|-------------|
| **Buy X Pay Y** | `BUY_X_PAY_X` | e.g. Buy 3, Pay 2. Applies to specific items. |
| **Category Discount** | `CATEGORY_DISCOUNT` | Percentage discount applied only to items in a specific category. |
| **Total Amount Discount** | `TOTAL_DISCOUNT` | Percentage discount applied when cart subtotal exceeds a certain amount. |

### Shipping

| Condition | Shipping Fee |
|-----------|-------------|
| Grand total ≥ 50 TL | Free (0.00 TL) |
| Grand total < 50 TL | 10.00 TL |

Shipping is calculated after the campaign discount is applied.

## Architecture

```
src/main/java/com/ecommerce/order_api/
├── controller/         # OrderController
├── service/            
│   ├── OrderService    # Order creation, stock check, financial calculations
│   ├── CampaignEngine  # Evaluates active campaigns, picks the best one
│   └── strategy/       # Strategy Pattern Implementation
│       ├── CampaignStrategy              # Contract for all campaigns
│       ├── BuyXGetYStrategy              # Buy X Pay Y implementation
│       ├── CategoryDiscountStrategy      # Category based % discount
│       └── TotalAmountDiscountStrategy   # Cart total based % discount
├── entity/             # Order, OrderItem, Product, Campaign, etc.
├── dto/                # OrderRequest, OrderResponse (Record classes)
├── repository/         # Spring Data JPA Repositories (with @Cacheable)
├── security/           # ApiKeyFilter for Authentication
├── exception/          # GlobalExceptionHandler, InsufficientStockException
└── seeder/             # DataSeeder (CommandLineRunner for initial data)
```

### Adding a New Campaign

1. Create a class implementing `CampaignStrategy` inside the `strategy/` package.
2. Annotate the class with `@Component` so Spring registers it as a bean.
3. Implement `getCampaignType()` and `calculateDiscount()`.
4. Add your new campaign type to CampaignType enum.
5. Done — The `CampaignEngine` will automatically inject and evaluate it!

```java
@Component
public class MyNewCampaignStrategy implements CampaignStrategy {

    @Override
    public CampaignType getCampaignType() { 
        return CampaignType.MY_NEW_CAMPAIGN; 
    }

    @Override
    public BigDecimal calculateDiscount(List<OrderItem> items, Campaign campaign) {
        // Implementation logic parsing campaign.getRuleDetails()
        return discountAmount;
    }
}
```

```java
public enum CampaignType {
    TOTAL_DISCOUNT,
    CATEGORY_DISCOUNT,
    BUY_X_PAY_X,
    MY_NEW_CAMPAİGN // <- New campaign type added
}
```

### Order Creation Flow

```
POST /api/v1/orders
  └── Validate Request payload (@Valid DTOs)
  └── Check Authentication (ApiKeyFilter)
  └── Begin Transaction
  └── Check stock for each requested item
  └── Deduct stock and save Product
  └── Create OrderItems and calculate Subtotal
  └── CampaignEngine.evaluateBestCampaign()
        ├── Fetch active campaigns (from Cache)
        ├── BuyXGetYStrategy.calculateDiscount()
        ├── CategoryDiscountStrategy.calculateDiscount()
        └── TotalAmountDiscountStrategy.calculateDiscount()
        └── Return highest discount
  └── Apply Discount
  └── Shipping Calculation: (Subtotal - Discount) < 50 TL → +10 TL
  └── Save Order
  └── Map to OrderResponse DTO
```
