/**
 * Payment Types - Backend API ile uyumlu
 */

export type Currency = 'TRY' | 'USD' | 'EUR' | 'GBP';

export type PaymentStatus = 
  | 'PENDING' 
  | 'PROCESSING' 
  | 'SUCCESS' 
  | 'FAILED' 
  | 'CANCELLED' 
  | 'REFUNDED';

export interface CardInfoDto {
  cardHolderName: string;
  cardNumber: string;
  expireMonth: string;
  expireYear: string;
  cvv: string;
}

export interface CreatePaymentRequest {
  conversationId: string;
  amount: number;
  currency: Currency;
  buyerId: string;
  cardInfo: CardInfoDto;
}

export interface PaymentResponse {
  id: string;
  conversationId: string;
  amount: number;
  currency: Currency;
  status: PaymentStatus;
  buyerId: string;
  createdAt: string;
  externalPaymentId?: string;
  errorMessage?: string;
  errorCode?: string;
}

export interface ProblemDetail {
  type?: string;
  title: string;
  status: number;
  detail: string;
  timestamp?: string;
  errorCode?: string;
  errors?: Record<string, string>;
}

