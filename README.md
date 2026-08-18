# Order & Campaign Service

A REST API built with Spring Boot 3 for managing shopping carts, evaluating campaign discounts dynamically, and processing orders.

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

**API base URL:** `http://localhost:8080/api/v1`

## Authentication

All endpoints require an `x-api-key` header to prevent unauthorized access.

| Token | Description |
|-------|------------|
| `VerySecretApiKey123` | Default API key configured in application.properties |

```http
x-api-key: VerySecretApiKey123
```

## API Endpoints

### Carts

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/carts/add-items` | Add items to a shopping cart. Auto-creates cart if it doesn't exist and evaluates campaigns on the fly. |

### Orders

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/orders/from-cart/{sessionId}` | Checkout flow. Converts a cart into an order, deducts stock, and deletes the cart. |
| `POST` | `/api/v1/orders` | Create an order directly bypassing the cart (Legacy method) |
| `GET`  | `/api/v1/orders/{id}` | Get detailed order information including financial breakdown |

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
├── controller/         # OrderController, CartController
├── service/            
│   ├── OrderService    # Order creation, checkout flow
│   ├── CartService     # Cart management, item upserts, lazy initialization
│   ├── CampaignEngine  # Evaluates active campaigns, picks the best one
│   └── strategy/       # Strategy Pattern Implementation
│       ├── CampaignStrategy
│       ├── BuyXGetYStrategy
│       ├── CategoryDiscountStrategy
│       └── TotalAmountDiscountStrategy
├── entity/             # Order, Cart, CartItem, Product, Campaign, etc.
├── dto/                # Request/Response DTOs (Record classes)
├── repository/         # Spring Data Repositories (Order, Cart, Product, Campaign)
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

### Cart Add Items Flow (Lazy Upsert)

```
POST /api/v1/carts/add-items
  └── Find cart by Session ID
  └── If not found → Create new Cart
  └── For each requested item:
        ├── Check if stock >= (cart quantity + requested quantity)
        ├── If item exists in cart → Increment quantity
        └── If item does not exist → Add new CartItem
  └── Save Cart
  └── CampaignEngine.evaluateBestCampaign() (On-the-fly calculation)
  └── Return CartResponse DTO with calculated financial totals
```

### Checkout Flow (Cart to Order)

```
POST /api/v1/orders/from-cart/{sessionId}
  └── Find Cart by Session ID
  └── Validate cart is not empty
  └── Begin Transaction
  └── For each CartItem:
        ├── Verify stock again (stock could change while sitting in cart)
        ├── Deduct stock and save Product (Permanent deduction)
        └── Convert CartItem to OrderItem
  └── Calculate Subtotal
  └── CampaignEngine.evaluateBestCampaign() → Apply highest discount
  └── Shipping Calculation: (Subtotal - Discount) < 50 TL → +10 TL
  └── Save Order
  └── Delete Cart (Cascade delete CartItems)
  └── Return OrderResponse DTO
```
