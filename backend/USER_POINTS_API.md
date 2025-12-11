# User Points API Documentation

## Overview
Kullanıcı puan yönetim sistemi. Kullanıcılar başarılı ödemeler sonrası puan kazanır ve bu puanları gelecek ödemelerde kullanabilir.

## Architecture
DDD multi-module Maven yapısı kullanılmıştır:
- **Domain Layer** (`payment-domain`): UserPoints entity ve business logic
- **Application Layer** (`payment-application`): UserPointsService ve DTOs
- **Infrastructure Layer** (`payment-infrastructure`): JPA implementation ve database
- **Interfaces Layer** (`payment-interfaces`): REST API endpoints

## Database Schema

### user_points Table
```sql
user_id VARCHAR(100) PRIMARY KEY
total_points DECIMAL(19,2) -- Toplam kazanılan puan (asla azalmaz)
available_points DECIMAL(19,2) -- Kullanılabilir puan
locked_points DECIMAL(19,2) -- Kilitli puan (bekleyen işlemler için)
created_at TIMESTAMP
last_updated TIMESTAMP
```

### Business Rules
- `total_points = available_points + locked_points + consumed_points`
- Puanlar BigDecimal ile yönetilir (güvenli para hesaplaması)
- Negatif puan olamaz
- Yeterli kullanılabilir puan yoksa harcama yapılamaz

## API Endpoints

### 1. Kullanıcı Puan Sorgulama
**GET** `/api/v1/user-points/{userId}`

Kullanıcının mevcut puan durumunu getirir. Eğer kullanıcının puan kaydı yoksa otomatik olarak 0 puan ile oluşturulur.

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

### 2. Puan Kazanma
**POST** `/api/v1/user-points/earn`

Başarılı ödeme sonrası kullanıcıya puan ekler.

**Request Body:**
```json
{
  "userId": "user123",
  "points": 50.00,
  "reason": "Payment completed successfully"
}
```

**Response:**
```json
{
  "userId": "user123",
  "totalPoints": 200.00,
  "availablePoints": 170.00,
  "lockedPoints": 30.00,
  "createdAt": "2024-01-15T10:30:00",
  "lastUpdated": "2024-01-21T09:15:00"
}
```

### 3. Puan Harcama
**POST** `/api/v1/user-points/spend`

Ödeme sırasında kullanıcının puanlarını harcar.

**Request Body:**
```json
{
  "userId": "user123",
  "points": 20.00,
  "reason": "Used in payment #12345"
}
```

**Response:**
```json
{
  "userId": "user123",
  "totalPoints": 200.00,
  "availablePoints": 150.00,
  "lockedPoints": 30.00,
  "createdAt": "2024-01-15T10:30:00",
  "lastUpdated": "2024-01-21T10:00:00"
}
```

**Error Response (Insufficient Points):**
```json
{
  "title": "Insufficient Points",
  "status": 400,
  "detail": "Insufficient available points. Available: 150.00, Requested: 200.00",
  "timestamp": "2024-01-21T10:00:00Z"
}
```

### 4. Puan Yeterlilik Kontrolü
**GET** `/api/v1/user-points/{userId}/check/{requiredPoints}`

Kullanıcının belirtilen miktarda puana sahip olup olmadığını kontrol eder.

**Example:** `GET /api/v1/user-points/user123/check/50.00`

**Response:**
```json
true
```

### 5. Health Check
**GET** `/api/v1/user-points/health`

Servis durumu kontrolü.

**Response:**
```
User points service is running
```

## Domain Model

### UserPoints Entity

**Business Methods:**
```java
// Puan kazanma
userPoints.earnPoints(BigDecimal.valueOf(50));

// Puan harcama
userPoints.spendPoints(BigDecimal.valueOf(20));

// Puan kilitleme (bekleyen işlemler için)
userPoints.lockPoints(BigDecimal.valueOf(30));

// Kilidi açma (işlem iptal)
userPoints.unlockPoints(BigDecimal.valueOf(30));

// Kilitli puanları tüket (işlem onay)
userPoints.consumeLockedPoints(BigDecimal.valueOf(30));

// Yeterlilik kontrolü
boolean hasEnough = userPoints.hasEnoughPoints(BigDecimal.valueOf(100));
```

## Error Handling

### Exception Types
1. **UserPointsNotFoundException** (404)
   - Kullanıcının puan kaydı bulunamadığında
   
2. **InsufficientPointsException** (400)
   - Yeterli kullanılabilir puan olmadığında
   
3. **IllegalArgumentException** (400)
   - Geçersiz puan miktarı (negatif veya sıfır)
   
4. **IllegalStateException** (409)
   - Business rule ihlali (örn: kilitli olandan fazla puan açma)

## Integration Examples

### Ödeme Sonrası Puan Kazandırma
```java
@Transactional
public PaymentResponse createPayment(CreatePaymentRequest request) {
    // Ödeme işlemi
    Payment payment = processPayment(request);
    
    // Başarılı ödeme sonrası puan kazandır
    if (payment.getStatus() == PaymentStatus.SUCCESS) {
        BigDecimal earnedPoints = calculatePoints(payment.getAmount());
        
        EarnPointsRequest earnRequest = new EarnPointsRequest();
        earnRequest.setUserId(payment.getBuyerId());
        earnRequest.setPoints(earnedPoints);
        earnRequest.setReason("Payment completed: " + payment.getId());
        
        userPointsService.earnPoints(earnRequest);
    }
    
    return mapToResponse(payment);
}
```

### Ödeme Sırasında Puan Kullanma
```java
@Transactional
public PaymentResponse payWithPoints(PaymentWithPointsRequest request) {
    // Puan yeterlilik kontrolü
    boolean hasEnough = userPointsService.hasEnoughPoints(
        request.getUserId(), 
        request.getPointsToUse()
    );
    
    if (!hasEnough) {
        throw new InsufficientPointsException("Not enough points");
    }
    
    // Puanları harca
    SpendPointsRequest spendRequest = new SpendPointsRequest();
    spendRequest.setUserId(request.getUserId());
    spendRequest.setPoints(request.getPointsToUse());
    
    userPointsService.spendPoints(spendRequest);
    
    // Ödeme işlemi devam eder...
}
```

## Security Considerations

### Data Privacy
- ✅ User ID loglanır (hassas değil)
- ✅ Puan miktarları loglanır
- ❌ Puan işlem gerekçeleri loglarda gizlenir (PII içerebilir)

### Validation
- ✅ Tüm input'lar API boundary'de validate edilir
- ✅ Domain katmanında business rule validation
- ✅ Negatif puan kontrolü
- ✅ Yeterlilik kontrolü

### Transaction Management
- ✅ Tüm puan işlemleri @Transactional içinde
- ✅ ACID garantisi
- ✅ Optimistic locking kullanılabilir (gelecek iyileştirme)

## Testing Scenarios

### Postman Collection'da Bulunması Gerekenler:
1. **Happy Path: Puan Kazanma ve Kullanma**
   - 1.1 - GET User Points (yeni kullanıcı, 0 puan dönmeli)
   - 1.2 - POST Earn Points (50 puan ekle)
   - 1.3 - GET User Points (50 puan görmeli)
   - 1.4 - POST Spend Points (20 puan harca)
   - 1.5 - GET User Points (30 puan kalmalı)

2. **Error Scenario: Yetersiz Puan**
   - 2.1 - GET User Points (mevcut durumu al)
   - 2.2 - POST Spend Points (mevcut + 100, hata dönmeli)
   - 2.3 - Verify Error Response (400, InsufficientPoints)

3. **Validation Scenario: Invalid Input**
   - 3.1 - POST Earn Points (negatif puan, 400 dönmeli)
   - 3.2 - POST Spend Points (sıfır puan, 400 dönmeli)

## Migration

### Database Migration Script
Flyway migration: `V3__create_user_points_table.sql`

Yeni deployment'ta otomatik olarak çalışır:
```bash
mvn flyway:migrate -pl payment-infrastructure
```

## Monitoring

### Key Metrics
- Toplam aktif kullanıcı sayısı
- Ortalama kullanıcı puanı
- Günlük puan kazanma/harcama miktarı
- Yetersiz puan error rate

### Logs
```
INFO - Querying points for user: user123
INFO - Earning points for user: user123, amount: 50.00
INFO - Spending points for user: user123, amount: 20.00
WARN - Insufficient points: Available: 30.00, Requested: 100.00
```

## Future Enhancements

1. **Puan Expiry**: Belirli süre sonra puanların sona ermesi
2. **Points History**: Puan işlem geçmişi tablosu
3. **Tiered Rewards**: Farklı müşteri seviyelerine göre farklı puan oranları
4. **Bonus Campaigns**: Özel kampanya puanları
5. **Points Transfer**: Kullanıcılar arası puan transferi

---

**DDD Compliance:** ✅  
**Security Rules:** ✅  
**Multi-Module Structure:** ✅  
**BigDecimal for Points:** ✅
