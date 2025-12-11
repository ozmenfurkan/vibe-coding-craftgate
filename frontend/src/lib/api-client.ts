import axios, { AxiosError } from 'axios';
import type { CreatePaymentRequest, PaymentResponse, ProblemDetail } from '@/types/payment.types';

const apiClient = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - Add idempotency key
apiClient.interceptors.request.use((config) => {
  // İdempotency key ekleme (conversation ID kullanılabilir)
  if (config.method === 'post' && config.data?.conversationId) {
    config.headers['Idempotency-Key'] = config.data.conversationId;
  }
  return config;
});

// Response interceptor - Error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ProblemDetail>) => {
    // Backend'den gelen structured error'ları handle et
    if (error.response?.data) {
      const problemDetail = error.response.data;
      // ProblemDetail formatında hata varsa, daha iyi mesaj göster
      throw new Error(problemDetail.detail || problemDetail.title);
    }
    throw error;
  }
);

export const paymentApi = {
  /**
   * Create a new payment
   */
  createPayment: async (request: CreatePaymentRequest): Promise<PaymentResponse> => {
    const response = await apiClient.post<PaymentResponse>('/payments', request);
    return response.data;
  },

  /**
   * Get payment by ID
   */
  getPayment: async (paymentId: string): Promise<PaymentResponse> => {
    const response = await apiClient.get<PaymentResponse>(`/payments/${paymentId}`);
    return response.data;
  },

  /**
   * Get payment by conversation ID
   */
  getPaymentByConversationId: async (conversationId: string): Promise<PaymentResponse | null> => {
    try {
      const response = await apiClient.get<PaymentResponse>(
        `/payments/by-conversation/${conversationId}`
      );
      return response.data;
    } catch (error) {
      // 404 ise null dön
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },
};

export default apiClient;

