-- Add provider column for multi-gateway support
-- Migration V2

ALTER TABLE payments 
ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'CRAFTGATE';

-- Index for filtering by provider
CREATE INDEX idx_provider ON payments(provider);

-- Comment
COMMENT ON COLUMN payments.provider IS 'Payment gateway provider (CRAFTGATE, AKBANK)';

