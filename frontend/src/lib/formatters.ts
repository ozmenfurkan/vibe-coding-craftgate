import type { Currency } from '@/types/payment.types';

/**
 * Format para miktarı - Intl.NumberFormat kullan (kurallar gereği)
 * String manipulation YAPMA!
 */
export function formatCurrency(amount: number, currency: Currency): string {
  return new Intl.NumberFormat('tr-TR', {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
}

/**
 * Format card number with masking
 * Her 4 rakamda bir boşluk ekle
 */
export function formatCardNumber(value: string): string {
  // Sadece rakamları al
  const digits = value.replace(/\D/g, '');
  
  // Her 4 rakamda bir boşluk ekle
  const formatted = digits.match(/.{1,4}/g)?.join(' ') || '';
  
  return formatted.substring(0, 23); // Max 19 digit + 4 spaces
}

/**
 * Mask card number - Sadece son 4 haneyi göster
 */
export function maskCardNumber(cardNumber: string): string {
  const digits = cardNumber.replace(/\D/g, '');
  
  if (digits.length < 4) {
    return '****';
  }
  
  const lastFour = digits.slice(-4);
  const masked = '*'.repeat(Math.max(0, digits.length - 4));
  
  return formatCardNumber(masked + lastFour);
}

/**
 * Format expire date
 */
export function formatExpireDate(value: string): string {
  const digits = value.replace(/\D/g, '');
  
  if (digits.length <= 2) {
    return digits;
  }
  
  return `${digits.slice(0, 2)}/${digits.slice(2, 4)}`;
}

/**
 * Parse expire date to month and year
 */
export function parseExpireDate(value: string): { month: string; year: string } {
  const digits = value.replace(/\D/g, '');
  
  const month = digits.slice(0, 2).padStart(2, '0');
  const year = digits.length > 2 ? `20${digits.slice(2, 4)}` : '';
  
  return { month, year };
}

/**
 * Generate unique conversation ID
 */
export function generateConversationId(): string {
  const timestamp = Date.now();
  const random = Math.random().toString(36).substring(2, 9);
  return `ORDER-${timestamp}-${random}`;
}

/**
 * Get currency symbol
 */
export function getCurrencySymbol(currency: Currency): string {
  const symbols: Record<Currency, string> = {
    TRY: '₺',
    USD: '$',
    EUR: '€',
    GBP: '£',
  };
  return symbols[currency];
}

