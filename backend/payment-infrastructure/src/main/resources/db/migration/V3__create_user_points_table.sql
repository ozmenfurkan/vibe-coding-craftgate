-- Create user_points table
-- This table stores user loyalty points information

CREATE TABLE user_points (
    user_id VARCHAR(100) PRIMARY KEY,
    total_points DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    available_points DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    locked_points DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    
    -- Constraints
    CONSTRAINT chk_total_points_positive CHECK (total_points >= 0),
    CONSTRAINT chk_available_points_positive CHECK (available_points >= 0),
    CONSTRAINT chk_locked_points_positive CHECK (locked_points >= 0),
    CONSTRAINT chk_points_consistency CHECK (total_points = available_points + locked_points)
);

-- Create index for faster lookups
CREATE INDEX idx_user_points_user_id ON user_points(user_id);
CREATE INDEX idx_user_points_last_updated ON user_points(last_updated);

-- Add comments
COMMENT ON TABLE user_points IS 'Stores user loyalty points information';
COMMENT ON COLUMN user_points.user_id IS 'Unique identifier for the user';
COMMENT ON COLUMN user_points.total_points IS 'Total accumulated points (never decreases)';
COMMENT ON COLUMN user_points.available_points IS 'Points available for spending';
COMMENT ON COLUMN user_points.locked_points IS 'Points locked for pending transactions';
COMMENT ON COLUMN user_points.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN user_points.last_updated IS 'Timestamp of last points update';
