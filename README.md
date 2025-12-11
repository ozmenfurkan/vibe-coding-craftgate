# Craftgate Payment System - Full Stack

Production-ready payment system with **Craftgate** integration, built with **DDD Architecture** (Backend) and **Modern React** (Frontend).

## 🏗️ Architecture Overview

```
📦 Project Root
├── 🔙 backend/          # Spring Boot + DDD
│   ├── domain/         # Pure business logic
│   ├── application/    # Use cases
│   ├── infrastructure/ # Craftgate, JPA
│   └── interface/      # REST API
│
└── 🎨 frontend/         # React + TypeScript
    ├── components/     # UI Components
    ├── hooks/          # Custom hooks
    ├── lib/            # Utilities
    └── types/          # TypeScript types
```

## ✨ Features

### Backend (Spring Boot)
- ✅ **DDD Architecture** - Clean separation of concerns
- ✅ **Multi-Gateway Support** - Akbank POS & Craftgate integration
- ✅ **User Points System** - Loyalty points management
- ✅ **Idempotency** - Duplicate payment prevention
- ✅ **PCI-DSS Compliant** - Secure card handling
- ✅ **BigDecimal** - No floating-point errors
- ✅ **PostgreSQL** - Production-ready database
- ✅ **Flyway Migration** - Database versioning
- ✅ **Global Error Handling** - RFC 7807 Problem Details

### Frontend (React)
- ✅ **TypeScript Strict Mode** - Full type safety
- ✅ **Zod Validation** - Luhn algorithm for cards
- ✅ **Auto Card Formatting** - Spaces every 4 digits
- ✅ **TanStack Query** - Server state management
- ✅ **React Hook Form** - Performant forms
- ✅ **TailwindCSS** - Modern, responsive UI
- ✅ **Currency Formatting** - Intl.NumberFormat

## 🚀 Quick Start

### Prerequisites

- **Java 17+**
- **Node.js 18+**
- **PostgreSQL 14+**
- **Maven 3.8+**
- **Craftgate Account** (Sandbox)

### 1. Database Setup

```bash
# Create database
psql -U postgres
CREATE DATABASE payment_db_dev;
\q
```

### 2. Backend Setup

```bash
cd backend

# Configure environment
cp .env.example .env
# Edit .env with your Craftgate credentials

# Run application
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend will start on `http://localhost:8080`

### 3. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Configure environment
cp .env.example .env

# Start development server
npm run dev
```

Frontend will start on `http://localhost:3000`

## 🧪 Testing

### Test Card Numbers (Craftgate Sandbox)

```
✅ Success:    5400010000000004
❌ Decline:    5400010000000012
🔒 3D Secure:  5400010000000020
```

### Test Payment

1. Open `http://localhost:3000`
2. Enter amount and select currency
3. Use test card: `5400010000000004`
4. Expire: `12/2030`, CVV: `123`
5. Click "Pay Now"

## 📡 API Documentation

### Payment API

#### Create Payment

```bash
POST /api/v1/payments
Content-Type: application/json
Idempotency-Key: unique-key-123

{
  "conversationId": "ORDER-12345",
  "amount": 100.50,
  "currency": "TRY",
  "buyerId": "buyer-123",
  "cardInfo": {
    "cardHolderName": "JOHN DOE",
    "cardNumber": "5400010000000004",
    "expireMonth": "12",
    "expireYear": "2030",
    "cvv": "123"
  }
}
```

### Response (Success)

```json
{
  "id": "uuid",
  "conversationId": "ORDER-12345",
  "amount": 100.50,
  "currency": "TRY",
  "status": "SUCCESS",
  "buyerId": "buyer-123",
  "createdAt": "2024-01-15T10:30:00",
  "externalPaymentId": "12345678"
}
```

### Response (Failed)

```json
{
  "id": "uuid",
  "conversationId": "ORDER-12345",
  "amount": 100.50,
  "currency": "TRY",
  "status": "FAILED",
  "buyerId": "buyer-123",
  "createdAt": "2024-01-15T10:30:00",
  "errorCode": "INSUFFICIENT_FUNDS",
  "errorMessage": "Insufficient funds"
}
```

### User Points API

#### Get User Points

```bash
GET /api/v1/user-points/{userId}
```

**Response:**
```json
{
  "userId": "user123",
  "totalPoints": 150.00,
  "availablePoints": 120.00,
  "lockedPoints": 30.00,
  "createdAt": "2024-01-15T10:30:00",
  "lastUpdated": "2024-01-20T14:45:00"
}
```

#### Earn Points

```bash
POST /api/v1/user-points/earn
Content-Type: application/json

{
  "userId": "user123",
  "points": 50.00,
  "reason": "Payment completed successfully"
}
```

#### Spend Points

```bash
POST /api/v1/user-points/spend
Content-Type: application/json

{
  "userId": "user123",
  "points": 20.00,
  "reason": "Used in payment"
}
```

#### Check Points Availability

```bash
GET /api/v1/user-points/{userId}/check/{requiredPoints}
```

**Response:** `true` or `false`

📚 **Detailed Documentation:** See [USER_POINTS_API.md](backend/USER_POINTS_API.md)

## 🔒 Security Rules

### ❌ NEVER DO THIS:

```java
// ❌ Don't log sensitive data
logger.info("Card: " + cardNumber);  // WRONG!

// ❌ Don't use float/double for money
double amount = 100.50;  // WRONG!

// ❌ Don't trust frontend input
String amount = request.getAmount();  // WRONG! Validate!
```

### ✅ DO THIS INSTEAD:

```java
// ✅ Log only safe information
logger.info("Processing payment. ConversationId: {}", conversationId);

// ✅ Use BigDecimal for money
BigDecimal amount = new BigDecimal("100.50");

// ✅ Validate everything at API boundary
@Valid @RequestBody CreatePaymentRequest request
```

## 💰 Money Handling

### Backend (Java)

```java
// ✅ CORRECT
Money money = new Money(new BigDecimal("100.50"), Currency.TRY);

// ❌ WRONG
double amount = 100.50;  // Causes rounding errors!
```

### Frontend (TypeScript)

```typescript
// ✅ CORRECT
formatCurrency(100.50, 'TRY')  // "₺100,50"

// ❌ WRONG
`${amount} TL`  // Don't manipulate strings!
```

## 📊 Project Statistics

```
Backend:
  - 30+ Java classes
  - 4 DDD layers
  - 100% type-safe
  - Zero tolerance for sensitive data logging

Frontend:
  - 15+ React components
  - Full TypeScript strict mode
  - Luhn validation
  - Auto card formatting
  - Currency formatting with Intl API
```

## 🎯 Multi-Gateway Support

### Supported Payment Providers

- ✅ **Craftgate** - Turkish payment gateway
- ✅ **Akbank Sanal POS** - Real bank integration with 3D Secure
- ➕ Easy to add more!

### Example: Payment with Akbank

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "AKBANK",
    "amount": 100.50,
    "currency": "TRY",
    ...
  }'
```

## 🏭 Production Deployment

### Backend

```bash
# Build
mvn clean package -DskipTests

# Run with production profile
java -jar payment-interfaces/target/payment-interfaces-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### Frontend

```bash
# Build
npm run build

# Deploy dist/ folder to CDN or web server
```

## 📚 Documentation

- **Backend**: See `backend/README.md`
- **Frontend**: See `frontend/README.md`
- **API Docs**: See `docs/api-specs.md` (if exists)
- **DDD Rules**: See `02-backend-ddd/RULE.md`
- **Security Rules**: See `01-global-security/RULE.md`

## 🔄 Development Workflow

### 1. Backend Changes

```bash
cd backend
# Make changes
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. Frontend Changes

```bash
cd frontend
# Make changes
npm run dev
# Changes hot-reload automatically
```

### 3. Full Stack Testing

1. Start backend (port 8080)
2. Start frontend (port 3000)
3. Frontend proxies API calls to backend
4. Test payment flow end-to-end

## 🐳 Docker Support

```bash
# Start PostgreSQL
docker-compose up -d postgres

# Or start everything
docker-compose up -d
```

## 🤝 Contributing

### Rules

1. ✅ **Code & commits in English**
2. ✅ **Turkish comments only for complex business logic**
3. ✅ **Never log sensitive data**
4. ✅ **Use BigDecimal for money (backend)**
5. ✅ **Use Intl.NumberFormat for currency (frontend)**
6. ✅ **Follow DDD layers (backend)**
7. ✅ **Feature-based folders (frontend)**
8. ✅ **No `any` type (TypeScript)**

### Before Committing

```bash
# Backend
mvn clean test
mvn clean verify

# Frontend
npm run type-check
npm run lint
```

## 📄 License

Proprietary - All rights reserved

## 🆘 Support

### Common Issues

**Issue**: Backend won't start
- **Solution**: Check if PostgreSQL is running and database exists

**Issue**: Frontend can't connect to backend
- **Solution**: Ensure backend is running on port 8080

**Issue**: Payment fails with "CRAFTGATE_ERROR"
- **Solution**: Verify Craftgate API keys in `.env`

**Issue**: CORS errors
- **Solution**: Frontend dev server has proxy configured, use it

### Contact

For issues related to:
- **Architecture**: Review DDD rules in `/rules`
- **Security**: Review security rules in `/rules`
- **Craftgate API**: See [Craftgate Docs](https://developer.craftgate.io/)

---

**Built with ❤️ using DDD, Spring Boot, React, and TypeScript**

🔒 **PCI-DSS Compliant** | ✓ **Luhn Validated** | 💳 **Idempotent** | 🚀 **Production Ready**

