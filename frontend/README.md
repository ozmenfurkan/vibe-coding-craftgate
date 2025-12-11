# Payment Frontend - Craftgate Integration

Modern, secure, and user-friendly payment interface built with **React 18**, **TypeScript**, and **TailwindCSS**.

## 🚀 Features

- ✅ **Type-Safe** - Full TypeScript with strict mode
- ✅ **Form Validation** - Zod schemas with Luhn algorithm
- ✅ **Card Input Masking** - Automatic formatting (every 4 digits)
- ✅ **Currency Formatting** - Intl.NumberFormat (no string manipulation!)
- ✅ **TanStack Query** - Server state management
- ✅ **React Hook Form** - Performant form handling
- ✅ **TailwindCSS** - Modern, responsive UI
- ✅ **Loading States** - Skeleton loaders for better UX
- ✅ **Error Handling** - User-friendly error messages

## 🛠️ Tech Stack

- **React 18** - UI Library
- **TypeScript** - Type Safety
- **Vite** - Build Tool
- **TailwindCSS** - Styling
- **TanStack Query** - Data Fetching
- **React Hook Form** - Form Management
- **Zod** - Schema Validation
- **Axios** - HTTP Client

## 📦 Installation

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Configure Environment

Create `.env` file:

```bash
cp .env.example .env
```

Edit `.env`:
```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

### 3. Start Development Server

```bash
npm run dev
```

Application will start on `http://localhost:3000`

## 🎯 Usage

### Payment Form

The main payment form includes:

1. **Amount & Currency** - Enter payment amount and select currency
2. **Buyer ID** - Unique buyer identifier
3. **Card Information**:
   - Card Holder Name
   - Card Number (auto-formatted with spaces)
   - Expire Date (MM/YY format)
   - CVV (3-4 digits)

### Test Cards (Sandbox)

```
✅ Success:  5400010000000004
❌ Decline:  5400010000000012
🔒 3D Secure: 5400010000000020
```

## 🏗️ Project Structure

```
src/
├── components/
│   ├── payment/
│   │   ├── PaymentForm.tsx       # Main form component
│   │   ├── PaymentResult.tsx     # Success/Error display
│   │   ├── CardInput.tsx         # Auto-formatting card input
│   │   └── ExpireDateInput.tsx   # MM/YY input
│   └── ui/
│       ├── Input.tsx             # Base input component
│       └── Button.tsx            # Base button component
├── hooks/
│   └── usePayment.ts             # TanStack Query hooks
├── lib/
│   ├── api-client.ts             # Axios configuration
│   ├── query-client.ts           # TanStack Query setup
│   ├── validation.ts             # Zod schemas
│   ├── formatters.ts             # Currency & card formatters
│   └── utils.ts                  # Utility functions
├── types/
│   └── payment.types.ts          # TypeScript types
├── App.tsx                       # Main app component
└── main.tsx                      # Entry point
```

## 🔒 Security Features

### 1. Input Validation

- **Luhn Algorithm** - Credit card checksum validation
- **Regex Patterns** - Card number, CVV, expire date
- **Domain Validation** - All validation happens on both client & server

### 2. Data Handling

```typescript
// ✅ GOOD: Secure display
<p>Card: {maskCardNumber(card)}</p>  // Shows: ************1234

// ❌ BAD: Never log or display full card
console.log(cardNumber); // NEVER do this!
```

### 3. API Security

- **Idempotency-Key** header automatically added
- **HTTPS only** in production
- **No sensitive data in URLs** (always POST for payments)

## 💰 Currency Handling

```typescript
// ✅ CORRECT: Using Intl.NumberFormat
formatCurrency(100.50, 'TRY')  // "₺100,50"

// ❌ WRONG: String manipulation
`${amount} TL`  // Don't do this!
```

## 📱 Responsive Design

The UI is fully responsive:
- ✅ Mobile (320px+)
- ✅ Tablet (768px+)
- ✅ Desktop (1024px+)

## 🧪 Testing

### Run Type Check

```bash
npm run type-check
```

### Run Linter

```bash
npm run lint
```

## 🏭 Production Build

### Build for Production

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

## 🔄 API Integration

### Backend Connection

Frontend connects to backend via proxy (see `vite.config.ts`):

```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
}
```

### API Endpoints Used

```
POST   /api/v1/payments              # Create payment
GET    /api/v1/payments/:id          # Get payment by ID
GET    /api/v1/payments/by-conversation/:id  # Get by conversation ID
```

## 📚 Key Concepts

### Feature-Based Structure

Following frontend rules, files are organized by feature:

```
components/payment/    # Payment feature
components/ui/         # Reusable UI components
```

### Type Safety

Strict TypeScript configuration:

```json
{
  "strict": true,
  "noImplicitAny": true,
  "noExplicitAny": "error"
}
```

### Form Validation

Zod schemas ensure type-safe validation:

```typescript
const cardInfoSchema = z.object({
  cardNumber: z.string()
    .refine(validateCardNumberLuhn, {
      message: 'Invalid card number'
    })
});
```

## 🎨 Styling

### TailwindCSS

Utility-first CSS framework:

```tsx
<button className="px-4 py-2 bg-primary-600 text-white rounded-lg">
  Pay Now
</button>
```

### Custom Theme

Defined in `tailwind.config.js`:

```javascript
theme: {
  extend: {
    colors: {
      primary: { ... }
    }
  }
}
```

## ⚡ Performance

- **Code Splitting** - Automatic with Vite
- **Tree Shaking** - Removes unused code
- **Lazy Loading** - Components loaded on demand
- **Memoization** - React Hook Form optimizations

## 🐛 Common Issues

### Issue: CORS Error

**Solution**: Make sure backend is running on port 8080

### Issue: Validation Not Working

**Solution**: Check Zod schema matches backend DTO structure

### Issue: Card Formatting Not Working

**Solution**: Ensure `CardInput` component is used instead of regular `Input`

## 📄 License

Proprietary - All rights reserved

## 🤝 Contributing

Follow these rules:
1. All code in **English**
2. Use **TypeScript** (no `any` types!)
3. Follow **feature-based** folder structure
4. Use **Intl.NumberFormat** for currency
5. **Never** log sensitive data

## 🆘 Support

For issues:
- **Type Errors**: Check TypeScript configuration
- **API Errors**: Verify backend is running
- **Styling Issues**: Check TailwindCSS configuration

---

**Built with React 18, TypeScript, and TailwindCSS** 🚀

