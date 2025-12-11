import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { paymentFormSchema, type PaymentFormData } from '@/lib/validation';
import { useCreatePayment } from '@/hooks/usePayment';
import { generateConversationId, parseExpireDate } from '@/lib/formatters';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import CardInput from './CardInput';
import ExpireDateInput from './ExpireDateInput';
import PaymentResult from './PaymentResult';
import type { Currency } from '@/types/payment.types';
import type { PaymentResponse } from '@/types/payment.types';

interface PaymentFormProps {
  defaultAmount?: number;
  defaultCurrency?: Currency;
  defaultProvider?: PaymentProvider;
  defaultBuyerId?: string;
}

export default function PaymentForm({ 
  defaultAmount = 100, 
  defaultCurrency = 'TRY',
  defaultProvider = 'CRAFTGATE',
  defaultBuyerId = 'buyer-123' 
}: PaymentFormProps) {
  const [paymentResult, setPaymentResult] = useState<PaymentResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<PaymentFormData>({
    resolver: zodResolver(paymentFormSchema),
    defaultValues: {
      amount: defaultAmount,
      currency: defaultCurrency,
      provider: defaultProvider,
      buyerId: defaultBuyerId,
      cardInfo: {
        cardHolderName: '',
        cardNumber: '',
        expireMonth: '',
        expireYear: '',
        cvv: '',
      },
    },
  });

  const createPayment = useCreatePayment();

  const onSubmit = async (data: PaymentFormData) => {
    try {
      setError(null);
      setPaymentResult(null);

      // Generate unique conversation ID
      const conversationId = generateConversationId();

      // Parse expire date
      const { month, year } = parseExpireDate(
        `${data.cardInfo.expireMonth}${data.cardInfo.expireYear.slice(-2)}`
      );

      // Create payment request
      const response = await createPayment.mutateAsync({
        conversationId,
        amount: data.amount,
        currency: data.currency,
        provider: data.provider,
        buyerId: data.buyerId,
        cardInfo: {
          cardHolderName: data.cardInfo.cardHolderName.toUpperCase(),
          cardNumber: data.cardInfo.cardNumber,
          expireMonth: month,
          expireYear: data.cardInfo.expireYear,
          cvv: data.cardInfo.cvv,
        },
      });

      setPaymentResult(response);

      // Reset form on success
      if (response.status === 'SUCCESS') {
        reset();
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'An unexpected error occurred';
      setError(errorMessage);
    }
  };

  // Eğer payment result varsa, sonucu göster
  if (paymentResult) {
    return (
      <PaymentResult 
        payment={paymentResult} 
        onNewPayment={() => {
          setPaymentResult(null);
          setError(null);
          reset();
        }}
      />
    );
  }

  return (
    <div className="w-full max-w-2xl mx-auto">
      <div className="bg-white rounded-xl shadow-lg p-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-gray-900">Payment Gateway</h2>
          <p className="mt-2 text-gray-600">Secure payment with Craftgate</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          {/* Amount & Currency */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Amount"
              type="number"
              step="0.01"
              placeholder="100.00"
              error={errors.amount?.message}
              required
              {...register('amount', { valueAsNumber: true })}
            />

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Currency <span className="text-red-500 ml-1">*</span>
              </label>
              <select
                className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                {...register('currency')}
              >
                <option value="TRY">🇹🇷 Turkish Lira (TRY)</option>
                <option value="USD">🇺🇸 US Dollar (USD)</option>
                <option value="EUR">🇪🇺 Euro (EUR)</option>
                <option value="GBP">🇬🇧 British Pound (GBP)</option>
              </select>
              {errors.currency && (
                <p className="mt-1 text-sm text-red-600">{errors.currency.message}</p>
              )}
            </div>
          </div>

          {/* Buyer ID */}
          <Input
            label="Buyer ID"
            placeholder="buyer-123"
            error={errors.buyerId?.message}
            required
            {...register('buyerId')}
          />

          {/* Payment Provider */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Payment Provider <span className="text-red-500 ml-1">*</span>
            </label>
            <select
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              {...register('provider')}
            >
              <option value="CRAFTGATE">🏦 Craftgate</option>
              <option value="AKBANK">🏦 Akbank Sanal POS</option>
            </select>
            {errors.provider && (
              <p className="mt-1 text-sm text-red-600">{errors.provider.message}</p>
            )}
          </div>

          {/* Divider */}
          <div className="border-t border-gray-200 pt-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Card Information</h3>
          </div>

          {/* Card Holder Name */}
          <Input
            label="Card Holder Name"
            placeholder="JOHN DOE"
            error={errors.cardInfo?.cardHolderName?.message}
            required
            {...register('cardInfo.cardHolderName')}
          />

          {/* Card Number */}
          <Controller
            name="cardInfo.cardNumber"
            control={control}
            render={({ field }) => (
              <CardInput
                label="Card Number"
                error={errors.cardInfo?.cardNumber?.message}
                required
                {...field}
              />
            )}
          />

          {/* Expire Date & CVV */}
          <div className="grid grid-cols-2 gap-4">
            <Controller
              name="cardInfo.expireMonth"
              control={control}
              render={({ field }) => (
                <ExpireDateInput
                  label="Expire Date"
                  error={errors.cardInfo?.expireMonth?.message}
                  required
                  {...field}
                />
              )}
            />

            <Input
              label="CVV"
              type="password"
              maxLength={4}
              placeholder="123"
              inputMode="numeric"
              error={errors.cardInfo?.cvv?.message}
              required
              {...register('cardInfo.cvv')}
            />
          </div>

          {/* Test Card Info */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <p className="text-sm font-medium text-blue-900 mb-2">
              🧪 Test Card Numbers (Sandbox)
            </p>
            <ul className="text-xs text-blue-700 space-y-1">
              <li>✅ Success: <code className="bg-blue-100 px-1 rounded">5400010000000004</code></li>
              <li>❌ Decline: <code className="bg-blue-100 px-1 rounded">5400010000000012</code></li>
            </ul>
          </div>

          {/* Error Message */}
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4">
              <p className="text-sm font-medium text-red-800">
                ❌ {error}
              </p>
            </div>
          )}

          {/* Submit Button */}
          <Button
            type="submit"
            variant="primary"
            size="lg"
            className="w-full"
            loading={createPayment.isPending}
          >
            {createPayment.isPending ? 'Processing...' : 'Pay Now'}
          </Button>

          {/* Security Info */}
          <div className="flex items-center justify-center text-sm text-gray-500">
            <svg 
              className="w-4 h-4 mr-2" 
              fill="currentColor" 
              viewBox="0 0 20 20"
            >
              <path 
                fillRule="evenodd" 
                d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" 
                clipRule="evenodd" 
              />
            </svg>
            Secure payment powered by Craftgate
          </div>
        </form>
      </div>
    </div>
  );
}

