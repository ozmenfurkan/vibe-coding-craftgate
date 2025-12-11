import PaymentForm from '@/components/payment/PaymentForm';

function App() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 py-12 px-4">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="text-center mb-12">
          <h1 className="text-5xl font-bold text-gray-900 mb-4">
            Craftgate Payment Integration
          </h1>
          <p className="text-xl text-gray-600">
            Secure, fast, and reliable payment processing
          </p>
        </div>

        {/* Payment Form */}
        <PaymentForm 
          defaultAmount={100}
          defaultCurrency="TRY"
          defaultBuyerId="buyer-demo-123"
        />

        {/* Footer */}
        <div className="mt-12 text-center text-sm text-gray-500">
          <p>
            Built with React, TypeScript, TailwindCSS & DDD Architecture
          </p>
          <p className="mt-2">
            🔒 PCI-DSS Compliant | ✓ Luhn Validation | 💳 Idempotent Payments
          </p>
        </div>
      </div>
    </div>
  );
}

export default App;

