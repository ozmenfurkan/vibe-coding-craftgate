import { useMutation, useQuery } from '@tanstack/react-query';
import { paymentApi } from '@/lib/api-client';
import type { CreatePaymentRequest, PaymentResponse } from '@/types/payment.types';

/**
 * Hook for creating payment
 */
export function useCreatePayment() {
  return useMutation({
    mutationFn: (data: CreatePaymentRequest) => paymentApi.createPayment(data),
  });
}

/**
 * Hook for getting payment by ID
 */
export function usePayment(paymentId: string | null) {
  return useQuery({
    queryKey: ['payment', paymentId],
    queryFn: () => paymentApi.getPayment(paymentId!),
    enabled: !!paymentId,
  });
}

/**
 * Hook for getting payment by conversation ID
 */
export function usePaymentByConversationId(conversationId: string | null) {
  return useQuery({
    queryKey: ['payment', 'conversation', conversationId],
    queryFn: () => paymentApi.getPaymentByConversationId(conversationId!),
    enabled: !!conversationId,
  });
}

