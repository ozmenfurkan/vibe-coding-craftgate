# Payment Service with Craftgate Integration - Multi-Module DDD

A production-ready payment service built with **Spring Boot 3.x**, **Craftgate** payment gateway, following **Domain-Driven Design (DDD)** with **Multi-Module Maven** architecture.

## 🏗️ Multi-Module Architecture

```
backend/
├── pom.xml                      # Parent POM
├── payment-domain/              # ✅ Pure Java (NO dependencies)
│   ├── pom.xml
│   └── src/main/java/.../domain/
│       ├── model/              # Entities & Value Objects
│       └── repository/         # Repository Interfaces
│
├── payment-application/         # ✅ Use Cases (depends: domain)
│   ├── pom.xml
│   └── src/main/java/.../application/
│       ├── dto/                # Data Transfer Objects
│       └── service/            # Application Services
│
├── payment-infrastructure/      # ✅ External Systems (depends: domain, application)
│   ├── pom.xml
│   └── src/main/java/.../infrastructure/
│       ├── gateway/            # Craftgate Client
│       └── persistence/        # JPA Repositories
│
└── payment-interfaces/          # ✅ API Layer (depends: all modules)
    ├── pom.xml
    └── src/main/java/.../
        ├── interfaces/rest/    # REST Controllers
        └── PaymentApplication.java
```

## ✨ Module Dependencies

```
┌─────────────────┐
│  domain         │ ← Pure Java, no dependencies
│  - Payment      │    + PaymentProvider enum ✅
│  - Money        │
└────────┬────────┘
         │
┌────────▼────────┐
│  application    │ ← depends on: domain
│  - DTOs         │
│  - PaymentGatewayFactory ✅ (Strategy Pattern)
│  - PaymentGateway (interface) ✅
└────────┬────────┘
         │
┌────────▼────────┐
│ infrastructure  │ ← depends on: domain, application
│  - CraftgatePaymentGateway ✅
│  - AkbankPaymentGateway ✅ (GERÇEK API!)
│  - JPA          │
└────────┬────────┘
         │
┌────────▼────────┐
│  interfaces     │ ← depends on: all modules
│  - REST API     │
└─────────────────┘
```

## 🎯 Payment Gateway Support

### Supported Gateways

- ✅ **Craftgate** - Turkish payment gateway (Sandbox + Production)
- ✅ **Akbank Sanal POS** - Real bank integration with 3D Secure
- ➕ **Easy to add more** - Just implement `PaymentGateway` interface

### Provider Selection

```json
{
  "provider": "CRAFTGATE",  // or "AKBANK"
  "amount": 100.50,
  ...
}
```

Gateway Factory otomatik olarak doğru gateway'i seçer!

## 🚀 Quick Start

### 1. Build All Modules

```bash
cd backend

# Build from parent (builds all modules)
mvn clean install
```

### 2. Run Application

```bash
# From parent directory
mvn spring-boot:run -pl payment-interfaces

# Or from interfaces module
cd payment-interfaces
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. Package for Production

```bash
# From parent
mvn clean package

# JAR will be in payment-interfaces/target/
java -jar payment-interfaces/target/payment-interfaces-1.0.0-SNAPSHOT.jar
```

## 📦 Module Details

### 1. payment-domain (Pure Java)

**Purpose**: Business logic, entities, value objects  
**Dependencies**: NONE (only JUnit for tests)  
**Rules**: 
- ❌ NO Spring annotations
- ❌ NO external libraries
- ✅ Pure Java only

```java
// Example: Payment entity
public class Payment {
    private String id;
    private Money amount;  // Value Object with BigDecimal
    
    public void markAsSuccess(String externalPaymentId) {
        // Business logic HERE
    }
}
```

### 2. payment-application (Use Cases)

**Purpose**: Orchestrate use cases, DTOs  
**Dependencies**: `payment-domain`, Spring Boot Starter  
**Rules**:
- ✅ @Service, @Transactional allowed
- ✅ DTO ↔ Domain mapping
- ✅ Use case orchestration

```java
@Service
public class PaymentService {
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        // Orchestrate use case
    }
}
```

### 3. payment-infrastructure (External Systems)

**Purpose**: Database, external APIs  
**Dependencies**: `payment-domain`, `payment-application`, JPA, Craftgate  
**Rules**:
- ✅ JPA entities & repositories
- ✅ External API clients
- ✅ Database migrations (Flyway)

```java
@Repository
public class JpaPaymentRepository implements PaymentRepository {
    // Implement domain repository interface
}

@Component
public class CraftgatePaymentGateway implements PaymentGateway {
    // Craftgate integration
}
```

### 4. payment-interfaces (REST API)

**Purpose**: REST controllers, main application  
**Dependencies**: All other modules  
**Rules**:
- ✅ REST controllers
- ✅ Global error handling
- ✅ Main Spring Boot application
- ✅ application.yml configuration

```java
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        // Only DTOs, no domain objects
    }
}
```

## 🔧 Maven Commands

### Build Specific Module

```bash
# Build only domain
mvn clean install -pl payment-domain

# Build domain + application
mvn clean install -pl payment-domain,payment-application
```

### Run Tests

```bash
# All modules
mvn test

# Specific module
mvn test -pl payment-domain
```

### Skip Tests

```bash
mvn clean install -DskipTests
```

### Dependency Tree

```bash
# See all dependencies
mvn dependency:tree

# Specific module
mvn dependency:tree -pl payment-interfaces
```

## 🎯 Why Multi-Module?

### ✅ Advantages

1. **Clear Boundaries** - Each module has specific responsibility
2. **Enforce DDD** - Domain can't accidentally use Spring
3. **Reusability** - Domain module can be used in other projects
4. **Build Speed** - Only rebuild changed modules
5. **Testing** - Test each layer independently
6. **Team Work** - Different teams can work on different modules

### Example: Domain Module Cannot Use Spring

```java
// ❌ This won't compile in domain module
@Service  // Spring annotation not available!
public class Payment {
    @Autowired  // Compile error!
    private PaymentRepository repository;
}

// ✅ This works - pure Java
public class Payment {
    public void markAsSuccess(String externalPaymentId) {
        // Business logic only
    }
}
```

## 🔒 Security Features

Same security rules apply across all modules:
- ❌ Never log PAN, CVV, passwords
- ✅ Only masked card numbers in logs
- ✅ BigDecimal for money
- ✅ Idempotency checks
- ✅ Input validation at API boundary

## 📊 Module Sizes

After build:
- `payment-domain`: ~50KB (pure Java)
- `payment-application`: ~100KB
- `payment-infrastructure`: ~200KB (includes JPA, Craftgate)
- `payment-interfaces`: ~300KB (includes all dependencies)

## 🧪 Testing Strategy

### Unit Tests (Each Module)

```bash
# Domain - Business logic tests
cd payment-domain && mvn test

# Application - Use case tests
cd payment-application && mvn test
```

### Integration Tests (Interfaces)

```bash
# Full integration tests
cd payment-interfaces && mvn verify
```

## 🐳 Docker

```bash
# Build all modules first
mvn clean package

# Then docker build
docker build -t payment-service .
docker-compose up
```

## 📚 Best Practices

1. **Always build from parent** for consistency
2. **Domain stays pure** - no external dependencies
3. **Application orchestrates** - no business logic in controllers
4. **Infrastructure implements** - no business logic here either
5. **Interfaces thin** - just REST and wiring

## 🔄 Development Workflow

```bash
# 1. Start from parent
cd backend

# 2. Build all modules
mvn clean install

# 3. Run application
mvn spring-boot:run -pl payment-interfaces

# 4. Make changes in specific module
cd payment-domain
# Edit files...

# 5. Rebuild only changed module
mvn clean install

# 6. Restart application
cd ..
mvn spring-boot:run -pl payment-interfaces
```

## 📄 License

Proprietary - All rights reserved

---

**Built with Multi-Module Maven + DDD + Spring Boot 3.x** 🚀
