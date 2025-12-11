-- Initial Payment Schema
-- Flyway migration script

CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(100) NOT NULL UNIQUE,
    amount DECIMAL(19, 2) NOT NULL CHECK (amount >= 0),
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    buyer_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    external_payment_id VARCHAR(100),
    error_message VARCHAR(500),
    error_code VARCHAR(50),
    masked_card_number VARCHAR(20)
);

-- Indexes for performance
CREATE INDEX idx_conversation_id ON payments(conversation_id);
CREATE INDEX idx_external_payment_id ON payments(external_payment_id);
CREATE INDEX idx_buyer_id ON payments(buyer_id);
CREATE INDEX idx_created_at ON payments(created_at DESC);
CREATE INDEX idx_status ON payments(status);

-- Comments for documentation
COMMENT ON TABLE payments IS 'Payment transactions table';
COMMENT ON COLUMN payments.conversation_id IS 'Unique conversation ID for idempotency';
COMMENT ON COLUMN payments.amount IS 'Payment amount in decimal format (BigDecimal)';
COMMENT ON COLUMN payments.currency IS 'Currency code (TRY, USD, EUR, GBP)';
COMMENT ON COLUMN payments.status IS 'Payment status (PENDING, PROCESSING, SUCCESS, FAILED, CANCELLED, REFUNDED)';
COMMENT ON COLUMN payments.external_payment_id IS 'Craftgate payment ID';
COMMENT ON COLUMN payments.masked_card_number IS 'Masked card number for display (last 4 digits)';

