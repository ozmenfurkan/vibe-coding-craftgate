# Masterpass V2 Entegrasyonu

**Craftgate Confluence'a Dayalı Implementation**

## 📋 Genel Bakış

Masterpass V2 entegrasyonu, **Craftgate üzerinden server-side** ödeme akışı sağlar. Client-side UI tamamen merchant kontrolündedir.

### ✅ Tamamlanan Özellikler

- ✅ PaymentProvider enum'ına MASTERPASS eklendi
- ✅ Domain model Masterpass'i destekliyor
- ✅ MasterpassPaymentGateway implementasyonu
- ✅ Masterpass DTO'ları (Token, Complete)
- ✅ Environment-based configuration (secrets .env'den alınıyor)
- ✅ Scenario-based Postman collection

## 🏗️ Mimari (DDD Katmanları)

### **Domain Layer** (Pure Java - No Spring)
```
payment-domain/
└── model/
    └── PaymentProvider.java  ← MASTERPASS eklendi
```

### **Application Layer** (Use Cases)
```
payment-application/
└── dto/
    ├── MasterpassTokenRequest.java      ← Token generation
    ├── MasterpassTokenResponse.java     ← Token response
    └── MasterpassCompleteRequest.java   ← Payment complete
```

### **Infrastructure Layer** (External APIs)
```
payment-infrastructure/
└── gateway/
    └── masterpass/
        └── MasterpassPaymentGateway.java  ← Craftgate client (standard API)
```

**NOT:** Masterpass V2, Craftgate'in standart `CreatePaymentRequest` API'sini kullanır. Özel Masterpass sınıfları yoktur. Token generation ve complete işlemleri REST API üzerinden ayrı endpoint'ler ile yapılır.

### **Interface Layer** (REST API)
```
payment-interfaces/
└── resources/
    ├── application.yml         ← Masterpass config
    └── application-dev.yml     ← Dev/Test config
```

## 🔐 Configuration (Environment Variables)

### **application.yml**
```yaml
masterpass:
  enabled: ${MASTERPASS_ENABLED:true}
  api-key: ${MASTERPASS_API_KEY}
  secret-key: ${MASTERPASS_SECRET_KEY}
  base-url: ${MASTERPASS_BASE_URL:https://sandbox-api.craftgate.io}
  version: v2
```

### **.env Dosyası** (Secrets)
```bash
# Masterpass Credentials (Craftgate üzerinden)
MASTERPASS_ENABLED=true
MASTERPASS_API_KEY=sandbox-your-api-key
MASTERPASS_SECRET_KEY=sandbox-your-secret-key
MASTERPASS_BASE_URL=https://sandbox-api.craftgate.io
```

⚠️ **CRITICAL:** `.env` dosyası `.gitignore`'da olmalı!

## 🚀 Masterpass V2 Akışı (Confluence'dan)

### **1. Token Generation**
```http
POST /api/v1/masterpass/generate-token

{
  "msisdn": "905436636070",
  "userId": "CG_905436636070",
  "binNumber": "540669",
  "amount": 100.00,
  "currency": "TRY",
  "conversationId": "MP-001"
}

Response:
{
  "token": "xxx-yyy-zzz",
  "referenceId": "ref-123",
  "orderNo": "order-456",
  "terminalGroupId": "terminal-789"
}
```

**Merchant Backend → Craftgate → Masterpass API**
- Craftgate POS routing yapar
- Token MUST come from Craftgate (NOT client SDK!)

### **2. Client-Side: MFS.purchase()**
```javascript
// Client SDK call
MFS.purchase(token, orderNo, terminalGroupId);

// Returns responseCode:
// 0000 → No verification needed
// 5001 → OTP required
// 5010 → 3DS required
```

### **3. Complete Payment**
```http
POST /api/v1/masterpass/complete

{
  "referenceId": "ref-123",
  "conversationId": "MP-001",
  "token": "new-token-after-otp"
}

Response:
{
  "id": "payment-uuid",
  "status": "SUCCESS",
  "provider": "MASTERPASS",
  "externalPaymentId": "craftgate-payment-id"
}
```

## 🧪 Test Bilgileri (Sandbox)

### **Test OTP:** `123456` (tüm doğrulamalar için)

### **VPN Gereksinimi**
Sandbox test için **VPN bağlantısı zorunlu** (Confluence doc)

### **Test Kartları**
Her bankanın test POS'u ile eşleşen test kartları kullanılmalı:
- **Garanti BIN:** 540669
- **YKB BIN:** 404809

### **Production IP Whitelist**
Masterpass production için bu IP'ler banka tarafına tanımlanmalı:
- 185.188.37.1
- 212.2.217.54
- 195.33.224.194

## 📚 Postman Collection

**Collection:** `postman/Masterpass_V2_API.postman_collection.json`

### **Scenario Structure:**
```
0 - Setup & Health Check
  └── 0.1 - Health Check

1 - Happy Path: Non-3DS (OTP) Flow
  ├── 1.1 - Generate Token
  ├── 1.2 - Complete Payment (After OTP)
  └── 1.3 - Get Payment Status

2 - Alternative Flow: 3DS
  └── 2.1 - Generate Token (Force 3DS)

10 - Error Scenarios
  ├── 10.1 - Invalid MSISDN
  └── 10.2 - Missing Fields
```

### **Environment Variables:**
```
base_url = http://localhost:8080
test_msisdn = 905436636070
test_user_id = CG_905436636070
test_bin_garanti = 540669
test_bin_ykb = 404809
```

## 🔒 Güvenlik Kuralları (FinTech)

### ❌ ASLA LOGLAMA:
- ❌ MSISDN (phone number) → PII data
- ❌ Card information (PAN, CVV)
- ❌ OTP codes

### ✅ Güvenli Logging:
```java
// ✅ GOOD
logger.info("Masterpass payment started. ConversationId: {}", conversationId);

// ❌ BAD
logger.info("Masterpass payment for msisdn: {}", msisdn);  // NEVER!
```

## 🎯 Confluence Referansları

- **Ana Doküman:** Masterpass Integration (pageId: 1320255690)
- **Detaylar:** Masterpass Entegrasyonuna Ait Detaylar (pageId: 1338376193)
- **Puan:** Masterpass Puan Entegrasyonu (pageId: 849575944)

## ⚠️ Önemli Notlar

### **V2 Özellikleri:**
- ✅ Webhook-based (NO callback issues)
- ✅ No-response mechanism (otomatik iptal)
- ✅ Autopilot (POS failover)
- ✅ Tek panel yönetimi

### **V2 Limitasyonlar:**
- ❌ Puan kullanımı doğrudan desteklenmez (ayrı endpoint gerekli)
- ❌ 6 ay sonra iade manuel yapılmalı
- ❌ Retry yoktur (OTP/3DS sonrası)
- ❌ bankcardholdername bilgisi boş gelir

## 📊 Next Steps (Opsiyonel)

Eğer ileri özelliklere ihtiyaç varsa:

1. **3DS Init/Complete Endpoints**
   - `/masterpass/3ds-init`
   - `/masterpass/3ds-complete`

2. **Loyalty (Puan) Entegrasyonu**
   - `/masterpass/loyalties/retrieve`
   - Sadece YKB ve Garanti

3. **No-Response Job**
   - `/no-responses/resolve`
   - Cron job olarak çalışmalı

## 🚀 Çalıştırma

```bash
# .env dosyasını oluştur
cp .env.example .env

# Masterpass credentials ekle
vim .env

# Servisi başlat
mvn spring-boot:run -pl payment-interfaces

# Test
curl http://localhost:8080/actuator/health
```

## 📞 Destek

Sorularınız için:
- Craftgate Confluence: Masterpass Integration
- Jira Tasklari: `project = CG AND text ~ "Masterpass"`

---

**Implementation Date:** 2024-12-11  
**Version:** 1.0.0  
**Author:** Confluence-based DDD Implementation
