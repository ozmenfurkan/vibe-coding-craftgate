import { formatCurrency } from '@/lib/formatters';
import Button from '@/components/ui/Button';
import type { PaymentResponse } from '@/types/payment.types';

interface PaymentResultProps {
  payment: PaymentResponse;
  onNewPayment: () => void;
}

export default function PaymentResult({ payment, onNewPayment }: PaymentResultProps) {
  const isSuccess = payment.status === 'SUCCESS';
  const isFailed = payment.status === 'FAILED';

  return (
    <div className="w-full max-w-2xl mx-auto">
      <div className="bg-white rounded-xl shadow-lg p-8">
        {/* Success State */}
        {isSuccess && (
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-100 mb-6">
              <svg
                className="h-10 w-10 text-green-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>

            <h2 className="text-3xl font-bold text-gray-900 mb-2">
              Payment Successful!
            </h2>
            <p className="text-gray-600 mb-8">
              Your payment has been processed successfully.
            </p>

            <div className="bg-gray-50 rounded-lg p-6 mb-8 space-y-3">
              <div className="flex justify-between items-center">
                <span className="text-sm font-medium text-gray-600">Amount:</span>
                <span className="text-2xl font-bold text-gray-900">
                  {formatCurrency(payment.amount, payment.currency)}
                </span>
              </div>

              <div className="border-t border-gray-200 pt-3">
                <div className="flex justify-between items-center text-sm">
                  <span className="text-gray-600">Payment ID:</span>
                  <span className="font-mono text-gray-900">{payment.id}</span>
                </div>
              </div>

              {payment.externalPaymentId && (
                <div className="flex justify-between items-center text-sm">
                  <span className="text-gray-600">Transaction ID:</span>
                  <span className="font-mono text-gray-900">{payment.externalPaymentId}</span>
                </div>
              )}

              <div className="flex justify-between items-center text-sm">
                <span className="text-gray-600">Provider:</span>
                <span className="font-mono text-gray-900">{payment.provider}</span>
              </div>

              <div className="flex justify-between items-center text-sm">
                <span className="text-gray-600">Status:</span>
                <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  ✓ {payment.status}
                </span>
              </div>
            </div>

            <Button
              onClick={onNewPayment}
              variant="primary"
              size="lg"
              className="w-full"
            >
              Make Another Payment
            </Button>
          </div>
        )}

        {/* Failed State */}
        {isFailed && (
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-red-100 mb-6">
              <svg
                className="h-10 w-10 text-red-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </div>

            <h2 className="text-3xl font-bold text-gray-900 mb-2">
              Payment Failed
            </h2>
            <p className="text-gray-600 mb-4">
              Unfortunately, your payment could not be processed.
            </p>

            {payment.errorMessage && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
                <p className="text-sm font-medium text-red-800">
                  {payment.errorMessage}
                </p>
                {payment.errorCode && (
                  <p className="text-xs text-red-600 mt-1">
                    Error Code: {payment.errorCode}
                  </p>
                )}
              </div>
            )}

            <div className="bg-gray-50 rounded-lg p-6 mb-8 space-y-3">
              <div className="flex justify-between items-center">
                <span className="text-sm font-medium text-gray-600">Attempted Amount:</span>
                <span className="text-xl font-bold text-gray-900">
                  {formatCurrency(payment.amount, payment.currency)}
                </span>
              </div>

              <div className="border-t border-gray-200 pt-3">
                <div className="flex justify-between items-center text-sm">
                  <span className="text-gray-600">Payment ID:</span>
                  <span className="font-mono text-gray-900">{payment.id}</span>
                </div>
              </div>

              <div className="flex justify-between items-center text-sm">
                <span className="text-gray-600">Status:</span>
                <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800">
                  ✗ {payment.status}
                </span>
              </div>
            </div>

            <Button
              onClick={onNewPayment}
              variant="primary"
              size="lg"
              className="w-full"
            >
              Try Again
            </Button>
          </div>
        )}

        {/* Other States (Pending, Processing, etc) */}
        {!isSuccess && !isFailed && (
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-yellow-100 mb-6">
              <svg
                className="animate-spin h-10 w-10 text-yellow-600"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                />
              </svg>
            </div>

            <h2 className="text-3xl font-bold text-gray-900 mb-2">
              Payment {payment.status}
            </h2>
            <p className="text-gray-600 mb-8">
              Please wait while we process your payment...
            </p>

            <div className="bg-gray-50 rounded-lg p-6 mb-8">
              <div className="flex justify-between items-center text-sm">
                <span className="text-gray-600">Payment ID:</span>
                <span className="font-mono text-gray-900">{payment.id}</span>
              </div>
            </div>

            <Button
              onClick={onNewPayment}
              variant="outline"
              size="lg"
              className="w-full"
            >
              Go Back
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}

