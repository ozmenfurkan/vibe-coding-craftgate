# Backend Multi-Gateway Pattern

## 🎯 Overview

Multiple payment gateway'leri (Craftgate, Akbank, etc.) destekleyen **Strategy Pattern** implementasyonu.

## 🏗️ Architecture

```
┌────────────────────────────────────────┐
│  PaymentService (Application Layer)   │
│  - Provider'ı request'ten alır         │
└────────────┬───────────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│  PaymentGatewayFactory                 │
│  - Provider → Gateway mapping          │
│  - Strategy pattern implementation     │
└────────────┬───────────────────────────┘
             │
        ┌────┴─────┐
        ▼          ▼
┌──────────┐  ┌──────────┐
│Craftgate │  │ Akbank   │
│Gateway   │  │Gateway   │
└──────────┘  └──────────┘
```

## 📦 Module Structure

### Domain Layer

```java
// PaymentProvider.java - Provider enum
public enum PaymentProvider {
    CRAFTGATE("Craftgate", "Turkish payment gateway"),
    AKBANK("Akbank Sanal POS", "Turkish bank virtual POS");
}

// Payment.java - Provider field eklendi
public class Payment {
    private PaymentProvider provider;
    
    public Payment(..., PaymentProvider provider, ...) {
        this.provider = provider;
    }
}
```

### Application Layer

```java
// PaymentGateway.java - Interface (tüm gateway'ler implement eder)
public interface PaymentGateway {
    String processPayment(Payment payment);
    String checkPaymentStatus(String externalPaymentId);
}

// PaymentGatewayFactory.java - Strategy selector
@Component
public class PaymentGatewayFactory {
    private final Map<PaymentProvider, PaymentGateway> gateways;
    
    public PaymentGateway getGateway(PaymentProvider provider) {
        return gateways.get(provider);
    }
}

// CreatePaymentRequest.java - Provider field
public class CreatePaymentRequest {
    @NotBlank
    @Pattern(regexp = "CRAFTGATE|AKBANK")
    private String provider;
}
```

### Infrastructure Layer

```java
// CraftgatePaymentGateway.java
@Component
public class CraftgatePaymentGateway implements PaymentGateway {
    @Override
    public String processPayment(Payment payment) {
        // Craftgate-specific implementation
    }
}

// AkbankPaymentGateway.java
@Component
@ConditionalOnProperty(name = "akbank.enabled", havingValue = "true")
public class AkbankPaymentGateway implements PaymentGateway {
    @Override
    public String processPayment(Payment payment) {
        // Akbank-specific implementation
    }
}
```

## 🔧 Usage

### 1. Request Example

```json
{
  "conversationId": "ORDER-12345",
  "amount": 100.50,
  "currency": "TRY",
  "provider": "AKBANK",  // ← Gateway seçimi
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

### 2. Service Flow

```java
@Service
public class PaymentService {
    private final PaymentGatewayFactory gatewayFactory;
    
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        // 1. DTO → Domain
        Payment payment = createPaymentFromRequest(request);
        
        // 2. Provider'a göre gateway seç
        PaymentGateway gateway = gatewayFactory.getGateway(
            payment.getProvider()
        );
        
        // 3. Gateway ile ödeme yap
        String externalId = gateway.processPayment(payment);
        
        // 4. Save & return
        payment.markAsSuccess(externalId);
        return mapToResponse(payment);
    }
}
```

## ➕ Yeni Gateway Ekleme

### Adım 1: Provider Enum'a Ekle

```java
public enum PaymentProvider {
    CRAFTGATE("Craftgate", "..."),
    AKBANK("Akbank Sanal POS", "..."),
    PAYTR("PayTR", "Turkish payment gateway");  // ← YENİ
}
```

### Adım 2: Gateway Implementation Oluştur

```java
@Component
@ConditionalOnProperty(name = "paytr.enabled", havingValue = "true")
public class PaytrPaymentGateway implements PaymentGateway {
    
    @Override
    public String processPayment(Payment payment) {
        // PayTR-specific logic
    }
    
    @Override
    public String checkPaymentStatus(String externalPaymentId) {
        // PayTR status check
    }
}
```

### Adım 3: Configuration Ekle

```yaml
# application.yml
paytr:
  enabled: ${PAYTR_ENABLED:false}
  merchant-id: ${PAYTR_MERCHANT_ID:}
  merchant-key: ${PAYTR_MERCHANT_KEY:}
  api-url: ${PAYTR_API_URL:https://www.paytr.com/odeme/api}
```

### Adım 4: Request DTO Validation Güncelle

```java
@Pattern(regexp = "CRAFTGATE|AKBANK|PAYTR")
private String provider;
```

**O kadar!** Factory otomatik olarak yeni gateway'i detect eder.

## 🔒 Conditional Gateway Loading

Gateway'ler conditional olarak yüklenebilir:

```java
@Component
@ConditionalOnProperty(name = "akbank.enabled", havingValue = "true")
public class AkbankPaymentGateway implements PaymentGateway {
    // Bu gateway sadece akbank.enabled=true ise yüklenir
}
```

### Configuration

```yaml
# Dev: Sadece Craftgate
craftgate:
  api-key: sandbox-key
  
akbank:
  enabled: false  # ← Akbank disabled

# Production: Her ikisi de aktif
craftgate:
  api-key: prod-key
  
akbank:
  enabled: true
  merchant-id: 12345
```

## 🧪 Testing

### Unit Test - Factory

```java
@Test
void shouldSelectCorrectGateway() {
    // Craftgate seçilmeli
    PaymentGateway gateway = factory.getGateway(PaymentProvider.CRAFTGATE);
    assertThat(gateway).isInstanceOf(CraftgatePaymentGateway.class);
    
    // Akbank seçilmeli
    gateway = factory.getGateway(PaymentProvider.AKBANK);
    assertThat(gateway).isInstanceOf(AkbankPaymentGateway.class);
}
```

### Integration Test

```java
@Test
void shouldProcessPaymentWithAkbank() {
    CreatePaymentRequest request = new CreatePaymentRequest();
    request.setProvider("AKBANK");  // ← Akbank seç
    request.setAmount(new BigDecimal("100.00"));
    
    PaymentResponse response = paymentService.createPayment(request);
    
    assertThat(response.getProvider()).isEqualTo("AKBANK");
    assertThat(response.getStatus()).isEqualTo("SUCCESS");
}
```

## ⚠️ Common Mistakes

### ❌ WRONG: Gateway'i hardcode etmek

```java
@Service
public class PaymentService {
    private final CraftgatePaymentGateway craftgate;  // ❌ Sadece Craftgate!
    
    public String pay() {
        return craftgate.processPayment(...);  // ❌ Akbank kullanılamaz!
    }
}
```

### ✅ CORRECT: Factory kullanmak

```java
@Service
public class PaymentService {
    private final PaymentGatewayFactory factory;  // ✅ Tüm gateway'ler
    
    public String pay(PaymentProvider provider) {
        PaymentGateway gateway = factory.getGateway(provider);  // ✅ Dynamic!
        return gateway.processPayment(...);
    }
}
```

## 📊 Database Schema

```sql
ALTER TABLE payments 
ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'CRAFTGATE';

CREATE INDEX idx_provider ON payments(provider);
```

## 🎯 Benefits

1. **Open/Closed Principle** - Yeni gateway eklemek için mevcut kodu değiştirmene gerek yok
2. **Strategy Pattern** - Runtime'da gateway seçimi
3. **Conditional Loading** - Sadece gerekli gateway'ler yüklenir
4. **Easy Testing** - Her gateway ayrı test edilebilir
5. **Provider Portability** - İstersen farklı customer'lara farklı gateway ver

## 📝 Checklist: Yeni Gateway Eklerken

- [ ] `PaymentProvider` enum'a ekle
- [ ] `PaymentGateway` interface'ini implement et
- [ ] `@Component` annotation ekle
- [ ] `@ConditionalOnProperty` ekle (optional)
- [ ] Configuration properties ekle (`application.yml`)
- [ ] DTO validation pattern'i güncelle
- [ ] Unit test yaz
- [ ] Integration test yaz
- [ ] README'ye gateway-specific dokümantasyon ekle

## 📚 References

- **Strategy Pattern**: https://refactoring.guru/design-patterns/strategy
- **Craftgate Docs**: https://developer.craftgate.io/
- **Akbank Sanal POS**: `docs/AKBANK_SANAL_POS_ENTEGRASYON_DOKÜMANI_V3_1.pdf`

---

**Multi-gateway architecture ile birden fazla ödeme sağlayıcısı kolayca yönetilebilir!** 🚀

