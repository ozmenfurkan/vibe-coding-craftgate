import { z } from 'zod';

/**
 * Luhn Algorithm - Kart numarası validasyonu
 * PCI-DSS standardına göre kart doğrulama
 */
export function validateCardNumberLuhn(cardNumber: string): boolean {
  const digits = cardNumber.replace(/\s/g, '');
  
  if (!/^\d{13,19}$/.test(digits)) {
    return false;
  }

  let sum = 0;
  let isEven = false;

  // Sondan başa doğru
  for (let i = digits.length - 1; i >= 0; i--) {
    let digit = parseInt(digits[i], 10);

    if (isEven) {
      digit *= 2;
      if (digit > 9) {
        digit -= 9;
      }
    }

    sum += digit;
    isEven = !isEven;
  }

  return sum % 10 === 0;
}

/**
 * Kart expire date validasyonu
 */
export function validateExpireDate(month: string, year: string): boolean {
  const currentDate = new Date();
  const currentYear = currentDate.getFullYear();
  const currentMonth = currentDate.getMonth() + 1;

  const expMonth = parseInt(month, 10);
  const expYear = parseInt(year, 10);

  if (expMonth < 1 || expMonth > 12) {
    return false;
  }

  if (expYear < currentYear) {
    return false;
  }

  if (expYear === currentYear && expMonth < currentMonth) {
    return false;
  }

  return true;
}

/**
 * Zod Schema for Card Info
 */
export const cardInfoSchema = z.object({
  cardHolderName: z
    .string()
    .min(2, 'Card holder name must be at least 2 characters')
    .max(100, 'Card holder name is too long')
    .regex(/^[a-zA-ZğüşıöçĞÜŞİÖÇ\s]+$/, 'Card holder name can only contain letters'),

  cardNumber: z
    .string()
    .min(13, 'Card number must be at least 13 digits')
    .max(19, 'Card number must not exceed 19 digits')
    .regex(/^[\d\s]+$/, 'Card number can only contain digits')
    .transform((val) => val.replace(/\s/g, '')) // Remove spaces
    .refine(validateCardNumberLuhn, {
      message: 'Invalid card number (failed Luhn check)',
    }),

  expireMonth: z
    .string()
    .regex(/^(0[1-9]|1[0-2])$/, 'Invalid month (use 01-12)'),

  expireYear: z
    .string()
    .regex(/^20[2-9]\d$/, 'Invalid year (format: 20XX)')
    .refine((year) => parseInt(year, 10) >= new Date().getFullYear(), {
      message: 'Card has expired',
    }),

  cvv: z
    .string()
    .regex(/^\d{3,4}$/, 'CVV must be 3 or 4 digits')
    .min(3, 'CVV must be at least 3 digits')
    .max(4, 'CVV must not exceed 4 digits'),
}).refine(
  (data) => validateExpireDate(data.expireMonth, data.expireYear),
  {
    message: 'Card has expired',
    path: ['expireMonth'], // Error'ı expire month field'ında göster
  }
);

/**
 * Zod Schema for Payment Form
 */
export const paymentFormSchema = z.object({
  amount: z
    .number()
    .positive('Amount must be greater than 0')
    .min(0.01, 'Minimum amount is 0.01')
    .max(999999.99, 'Amount is too large'),

  currency: z.enum(['TRY', 'USD', 'EUR', 'GBP'], {
    errorMap: () => ({ message: 'Please select a currency' }),
  }),

  buyerId: z
    .string()
    .min(1, 'Buyer ID is required')
    .max(100, 'Buyer ID is too long'),

  cardInfo: cardInfoSchema,
});

export type PaymentFormData = z.infer<typeof paymentFormSchema>;

