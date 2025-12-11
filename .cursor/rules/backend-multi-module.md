# Backend Multi-Module Maven + DDD Architecture

## 📦 Module Structure

Backend projelerinde **Multi-Module Maven** yapısı kullanılmalıdır. Her DDD layer'ı ayrı bir Maven module olarak organize edilir.

### Standard Module Hierarchy

```
backend/
├── pom.xml                          # Parent POM
│
├── {project}-domain/                # Domain Layer Module
│   ├── pom.xml                      # No external dependencies!
│   └── src/main/java/.../domain/
│       ├── model/                   # Entities & Value Objects
│       └── repository/              # Repository Interfaces
│
├── {project}-application/           # Application Layer Module
│   ├── pom.xml                      # Depends: domain only
│   └── src/main/java/.../application/
│       ├── dto/                     # Data Transfer Objects
│       └── service/                 # Application Services
│
├── {project}-infrastructure/        # Infrastructure Layer Module
│   ├── pom.xml                      # Depends: domain, application
│   └── src/main/java/.../infrastructure/
│       ├── gateway/                 # External API clients
│       ├── persistence/             # JPA implementations
│       └── config/                  # Infrastructure config
│
└── {project}-interfaces/            # Interface Layer Module
    ├── pom.xml                      # Depends: all modules
    └── src/main/java/.../
        ├── interfaces/rest/         # REST Controllers
        ├── {Project}Application.java # Main class
        └── resources/
            └── application.yml      # Configuration
```

## 🎯 Module Rules

### 1. Domain Module (Pure Java)

**POM Template:**
```xml
<artifactId>{project}-domain</artifactId>
<dependencies>
    <!-- NO EXTERNAL DEPENDENCIES! -->
    <!-- Only test dependencies -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Rules:**
- ❌ NO Spring annotations (`@Service`, `@Component`, etc.)
- ❌ NO external libraries (JPA, Jackson, etc.)
- ✅ Pure Java only
- ✅ Business logic ONLY
- ✅ Value Objects must be immutable
- ✅ Entities manage their own state

**What Goes Here:**
- Entities (Aggregate Roots)
- Value Objects (Money, Currency, etc.)
- Domain Events
- Repository Interfaces (no implementation!)
- Domain Services (pure business logic)
- Exceptions (domain-specific)

### 2. Application Module (Use Cases)

**POM Template:**
```xml
<artifactId>{project}-application</artifactId>
<dependencies>
    <dependency>
        <groupId>com.company</groupId>
        <artifactId>{project}-domain</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

**Rules:**
- ✅ @Service, @Transactional allowed
- ✅ DTO definitions
- ✅ DTO ↔ Domain mapping
- ✅ Use case orchestration
- ❌ NO business logic (that goes in domain!)
- ❌ NO external API calls (that goes in infrastructure!)

**What Goes Here:**
- Application Services
- DTOs (Request/Response)
- Use Case implementations
- Mappers (DTO ↔ Domain)
- Application-level exceptions
- **Gateway Interfaces** (infrastructure implements these!)

### 3. Infrastructure Module (External Systems)

**POM Template:**
```xml
<artifactId>{project}-infrastructure</artifactId>
<dependencies>
    <dependency>
        <groupId>com.company</groupId>
        <artifactId>{project}-domain</artifactId>
    </dependency>
    <dependency>
        <groupId>com.company</groupId>
        <artifactId>{project}-application</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <!-- External API SDKs -->
</dependencies>
```

**Rules:**
- ✅ JPA entities & repositories
- ✅ External API clients
- ✅ Database migrations (Flyway/Liquibase)
- ✅ Implements domain repository interfaces
- ✅ Mappers (JPA Entity ↔ Domain)
- ❌ NO business logic!

**What Goes Here:**
- JPA Entities
- JPA Repository implementations
- **External API Gateway IMPLEMENTATIONS** (interfaces are in application!)
- Mappers (Entity ↔ Domain)
- Database migrations (src/main/resources/db/migration/)

### 4. Interfaces Module (REST API)

**POM Template:**
```xml
<artifactId>{project}-interfaces</artifactId>
<dependencies>
    <!-- All other modules -->
    <dependency>
        <groupId>com.company</groupId>
        <artifactId>{project}-domain</artifactId>
    </dependency>
    <dependency>
        <groupId>com.company</groupId>
        <artifactId>{project}-application</artifactId>
    </dependency>
    <dependency>
        <groupId>com.company</groupId>
        <artifactId>{project}-infrastructure</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

**Rules:**
- ✅ REST Controllers
- ✅ Global Exception Handlers
- ✅ Main Spring Boot Application
- ✅ Configuration files (application.yml)
- ❌ NO business logic!
- ❌ Controllers only handle DTOs (never domain objects!)

**What Goes Here:**
- REST Controllers
- Global @ControllerAdvice
- Main Application class (@SpringBootApplication)
- Configuration (application.yml, application-{profile}.yml)
- Security configuration

## 🔧 Parent POM Template

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.company</groupId>
    <artifactId>{project}-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>{project}-domain</module>
        <module>{project}-application</module>
        <module>{project}-infrastructure</module>
        <module>{project}-interfaces</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Internal module versions -->
            <dependency>
                <groupId>com.company</groupId>
                <artifactId>{project}-domain</artifactId>
                <version>${project.version}</version>
            </dependency>
            <!-- Add other common dependencies -->
        </dependencies>
    </dependencyManagement>
</project>
```

## 🚀 Maven Commands

### Build All Modules
```bash
mvn clean install
```

### Build Specific Module
```bash
mvn clean install -pl {project}-domain
```

### Run Application
```bash
mvn spring-boot:run -pl {project}-interfaces
```

### Dependency Tree
```bash
mvn dependency:tree -pl {project}-interfaces
```

## ✅ Benefits of Multi-Module

1. **Enforces DDD Layers** - Domain can't accidentally use Spring
2. **Clear Dependencies** - Each module declares what it needs
3. **Reusability** - Domain module can be reused in other projects
4. **Build Speed** - Only rebuild changed modules
5. **Team Collaboration** - Teams can work on different modules
6. **Testing** - Test each layer independently

## 🔄 Dependency Inversion Principle

**CRITICAL:** Interface'ler üst layer'da, implementation'lar alt layer'da!

### ✅ CORRECT Pattern

```
payment-application/
└── src/main/java/.../application/
    └── gateway/
        └── PaymentGateway.java           # ✅ INTERFACE

payment-infrastructure/
└── src/main/java/.../infrastructure/
    └── gateway/
        └── craftgate/
            └── CraftgatePaymentGateway.java  # ✅ IMPLEMENTS
```

```java
// ✅ CORRECT: Interface in application
package com.company.project.application.gateway;

public interface PaymentGateway {
    String processPayment(Payment payment);
}

// ✅ CORRECT: Implementation in infrastructure
package com.company.project.infrastructure.gateway.craftgate;

import com.company.project.application.gateway.PaymentGateway;

@Component
public class CraftgatePaymentGateway implements PaymentGateway {
    @Override
    public String processPayment(Payment payment) {
        // Implementation
    }
}
```

### ❌ WRONG Pattern (Circular Dependency!)

```
payment-infrastructure/
└── src/main/java/.../infrastructure/
    └── gateway/
        └── PaymentGateway.java           # ❌ INTERFACE HERE!
        └── craftgate/
            └── CraftgatePaymentGateway.java
```

Bu durumda:
- Application → Infrastructure'a depend eder (interface için)
- Infrastructure → Application'a depend eder (PaymentService için)
- **CIRCULAR DEPENDENCY!** ❌

## ❌ Common Mistakes to Avoid

### 1. Wrong Dependencies

```xml
<!-- ❌ WRONG: Domain depends on Spring -->
<artifactId>project-domain</artifactId>
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
</dependencies>

<!-- ✅ CORRECT: Domain has no dependencies -->
<artifactId>project-domain</artifactId>
<dependencies>
    <!-- Only test dependencies -->
</dependencies>
```

### 2. Circular Dependencies

```xml
<!-- ❌ WRONG: Application depends on Infrastructure -->
<artifactId>project-application</artifactId>
<dependencies>
    <dependency>
        <artifactId>project-infrastructure</artifactId>
    </dependency>
</dependencies>

<!-- ✅ CORRECT: Application only depends on Domain -->
<artifactId>project-application</artifactId>
<dependencies>
    <dependency>
        <artifactId>project-domain</artifactId>
    </dependency>
</dependencies>
```

### 3. Business Logic in Wrong Place

```java
// ❌ WRONG: Business logic in Controller
@RestController
public class PaymentController {
    public ResponseEntity<PaymentResponse> pay() {
        if (amount > 1000) {
            // Business logic here!
        }
    }
}

// ✅ CORRECT: Business logic in Domain
public class Payment {
    public void validate() {
        if (this.amount.isGreaterThan(LIMIT)) {
            throw new PaymentLimitExceededException();
        }
    }
}
```

## 📝 Naming Conventions

- **Module Names:** `{project}-{layer}` (e.g., `payment-domain`)
- **Package Names:** `com.company.{project}.{layer}` (e.g., `com.dumensel.payment.domain`)
- **Artifact IDs:** Same as module names

## 🔒 Security Considerations

Even in multi-module setup:
- ❌ Never log sensitive data in ANY module
- ✅ Validate input at API boundary (interfaces module)
- ✅ Use BigDecimal for money in ALL modules
- ✅ Keep PII handling in domain logic

## 📚 References

- **Clean Architecture** by Robert C. Martin
- **Domain-Driven Design** by Eric Evans
- **Maven Multi-Module Projects** - Apache Maven Documentation

---

**Always follow this structure for new backend projects!** 🎯

