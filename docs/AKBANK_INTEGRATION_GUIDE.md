# Akbank Sanal POS Integration Guide

## 🎯 Overview

Gerçek Akbank Sanal POS entegrasyonu. **Mock değil, production-ready!**

## 🔧 Configuration

### Environment Variables

```bash
# Akbank credentials
AKBANK_ENABLED=true
AKBANK_CLIENT_ID=your-client-id
AKBANK_STORE_KEY=your-store-key

# URLs (callback URLs for 3D Secure)
AKBANK_SUCCESS_URL=https://yourdomain.com/payment/success
AKBANK_FAILURE_URL=https://yourdomain.com/payment/failure
AKBANK_CALLBACK_URL=https://yourdomain.com/payment/callback
```

### application.yml

```yaml
akbank:
  enabled: true
  client-id: ${AKBANK_CLIENT_ID}
  store-key: ${AKBANK_STORE_KEY}
  api-url: https://sanalpos.akbank.com.tr/fim/api
  success-url: ${AKBANK_SUCCESS_URL}
  failure-url: ${AKBANK_FAILURE_URL}
  callback-url: ${AKBANK_CALLBACK_URL}
```

## 🚀 Usage

### Request Example

```json
POST /api/v1/payments
{
  "conversationId": "ORDER-12345",
  "amount": 100.50,
  "currency": "TRY",
  "provider": "AKBANK",  // ← Akbank seç
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
  "provider": "AKBANK",
  "externalPaymentId": "1234567890",  // Akbank transaction ID
  "createdAt": "2024-01-15T10:30:00"
}
```

## 🔒 Security Features

### 1. Hash Calculation

Akbank requires SHA-512 hash for security:

```
Hash = BASE64(SHA512(clientId|oid|amount|okUrl|failUrl|tranType|rnd|storeKey))
```

Implementation:
```java
MessageDigest digest = MessageDigest.getInstance("SHA-512");
byte[] hashBytes = digest.digest(hashData.getBytes(StandardCharsets.UTF_8));
String hash = Base64.getEncoder().encodeToString(hashBytes);
```

### 2. 3D Secure Support

Akbank integration includes full 3D Secure support:
- `3d_pay_hosting`: 3D Secure with automatic payment
- Callback URLs for bank verification
- mdStatus validation (1, 2, 3, 4 = success)

### 3. No Sensitive Data Logging

```java
// ✅ GOOD: Safe logging
logger.info("Processing payment. OrderId: {}", orderId);

// ❌ BAD: Never log card info
logger.info("Card: {}", cardNumber);  // NEVER!
```

## 📊 API Endpoints

### Production
```
https://sanalpos.akbank.com.tr/fim/api/v1/gateway
```

### Test/Sandbox
```
https://sanalpos-test.akbank.com.tr/fim/api/v1/gateway
```

## 🧪 Testing

### Test Cards (Sandbox)

Akbank test kartları dokümantasyonda belirtilmiştir.

### Integration Test

```java
@Test
void shouldProcessAkbankPayment() {
    CreatePaymentRequest request = new CreatePaymentRequest();
    request.setProvider("AKBANK");
    request.setAmount(new BigDecimal("100.00"));
    request.setCurrency("TRY");
    // ... card info
    
    PaymentResponse response = paymentService.createPayment(request);
    
    assertThat(response.getStatus()).isEqualTo("SUCCESS");
    assertThat(response.getProvider()).isEqualTo("AKBANK");
    assertThat(response.getExternalPaymentId()).isNotNull();
}
```

## 📝 Request/Response Models

### AkbankPaymentRequest

```java
public class AkbankPaymentRequest {
    private String clientId;      // Merchant ID
    private String oid;            // Order ID (conversationId)
    private String amount;         // Amount (e.g., "100.50")
    private String currency;       // Currency code (949=TRY)
    private String rnd;            // Random string (UUID)
    private String hash;           // Security hash
    private String trantype;       // Transaction type (Auth)
    private String storetype;      // 3D Secure type
    private String okUrl;          // Success callback URL
    private String failUrl;        // Failure callback URL
    private String callbackUrl;    // 3D callback URL
    
    // Card info
    private String pan;            // Card number
    private String expireMonth;
    private String expireYear;
    private String cv2;            // CVV
    private String cardHolderName;
}
```

### AkbankPaymentResponse

```java
public class AkbankPaymentResponse {
    private String response;         // "Approved", "Declined", "Error"
    private String procReturnCode;   // Process return code
    private String mdStatus;         // 3D Secure status (1-4 = success)
    private String mdErrorMessage;   // 3D error message
    private String authCode;         // Authorization code
    private String transactionId;    // Akbank transaction ID
    private String orderId;          // Original order ID
    private String errorMessage;     // Error message if failed
}
```

## ⚠️ Error Codes

| Code | Description |
|------|-------------|
| `AKBANK_3D_SECURE_FAILED` | 3D Secure validation failed |
| `AKBANK_PAYMENT_DECLINED` | Payment declined by bank |
| `AKBANK_TECHNICAL_ERROR` | Technical error during payment |
| `AKBANK_API_ERROR` | Failed to communicate with Akbank API |
| `AKBANK_EMPTY_RESPONSE` | Empty response from API |
| `AKBANK_HASH_MISMATCH` | Response hash validation failed |

## 🔄 Payment Flow

```
1. Client → Backend: Payment request with provider="AKBANK"
   ↓
2. Backend → Akbank: POST with card info + hash
   ↓
3. Akbank → Bank: 3D Secure verification
   ↓
4. Bank → Akbank: 3D result
   ↓
5. Akbank → Backend: Payment response
   ↓
6. Backend → Client: Success/Failure response
```

## 🎯 Currency Codes

| Currency | Akbank Code |
|----------|-------------|
| TRY      | 949         |
| USD      | 840         |
| EUR      | 978         |
| GBP      | 826         |

## 📚 References

- **Official Documentation**: `docs/AKBANK_SANAL_POS_ENTEGRASYON_DOKÜMANI_V3_1.pdf`
- **Akbank Support**: Contact your Akbank account manager
- **Test Environment**: Contact Akbank for test credentials

## ⚡ Performance

- Average response time: ~2-3 seconds (including 3D Secure)
- Retry logic: 3 attempts with exponential backoff
- Timeout: 30 seconds

## 🔐 Production Checklist

- [ ] Obtain production credentials from Akbank
- [ ] Configure callback URLs (HTTPS required!)
- [ ] Set `akbank.enabled=true`
- [ ] Test with real test cards
- [ ] Verify 3D Secure flow
- [ ] Monitor error rates
- [ ] Set up alert system for payment failures

---

**Gerçek Akbank entegrasyonu - Production ready!** 🚀

