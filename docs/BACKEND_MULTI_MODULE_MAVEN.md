# Backend Multi-Module Maven + DDD Architecture

Bu dokümantasyon backend projeleri için **Multi-Module Maven** yapısını açıklar.

## 📦 Standard Module Structure

```
backend/
├── pom.xml                          # Parent POM
├── {project}-domain/                # Pure Java (NO deps)
├── {project}-application/           # Use Cases
├── {project}-infrastructure/        # JPA, External APIs
└── {project}-interfaces/            # REST API, Main App
```

## 🎯 Module Rules

### 1. Domain Module ✅ Pure Java

- ❌ NO Spring annotations
- ❌ NO external libraries
- ✅ Business logic ONLY
- ✅ Entities, Value Objects, Repository Interfaces

### 2. Application Module ✅ Use Cases

- ✅ @Service, @Transactional allowed
- ✅ DTOs & mapping
- ✅ **Gateway Interfaces** (infrastructure implements!)
- ❌ NO business logic (that's in domain!)

### 3. Infrastructure Module ✅ Technical Details

- ✅ JPA implementations
- ✅ External API clients
- ✅ **Gateway IMPLEMENTATIONS**
- ❌ NO business logic!

### 4. Interfaces Module ✅ API Layer

- ✅ REST Controllers
- ✅ Main @SpringBootApplication
- ✅ Configuration files
- ❌ Only DTOs, no domain objects!

## 🔄 Dependency Inversion

**CRITICAL:** Interface'ler üst layer'da, implementations alt layer'da!

```
payment-application/
└── gateway/
    └── PaymentGateway.java        # ✅ INTERFACE

payment-infrastructure/
└── gateway/
    └── craftgate/
        └── CraftgatePaymentGateway.java  # ✅ IMPLEMENTS
```

## 🚀 Maven Commands

```bash
# Build all modules
mvn clean install

# Run application
mvn spring-boot:run -pl {project}-interfaces

# Build specific module
mvn clean install -pl {project}-domain
```

## 📝 Naming Conventions

- Module: `{project}-{layer}` (örn: `payment-domain`)
- Package: `com.company.{project}.{layer}`

Detaylı bilgi için ilgili backend README dosyalarına bakın.

