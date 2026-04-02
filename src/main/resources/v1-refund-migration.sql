-- SCRUM-107: Refund and Cancellation Schema Update

-- 1. Update bookings table for room refunds
ALTER TABLE bookings ADD COLUMN refunded_amount DECIMAL(19, 2) DEFAULT 0 NOT NULL;
ALTER TABLE bookings ADD COLUMN cancelled_at TIMESTAMP;
ALTER TABLE bookings ADD COLUMN cancellation_reason VARCHAR(500);
ALTER TABLE bookings ADD COLUMN is_refundable BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE bookings ADD COLUMN payment_reference VARCHAR(100);

-- 2. Update adventure_checkout_bookings table
ALTER TABLE adventure_checkout_bookings ADD COLUMN refunded_amount DECIMAL(19, 2) DEFAULT 0 NOT NULL;
ALTER TABLE adventure_checkout_bookings ADD COLUMN cancelled_at TIMESTAMP;
ALTER TABLE adventure_checkout_bookings ADD COLUMN cancellation_reason VARCHAR(500);
ALTER TABLE adventure_checkout_bookings ADD COLUMN is_refundable BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE adventure_checkout_bookings ADD COLUMN payment_reference VARCHAR(100);

-- 3. Create cancellation_policies table
CREATE TABLE cancellation_policies (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(255) NOT NULL UNIQUE,
    full_refund_limit_hours INT NOT NULL,
    partial_refund_limit_hours INT NOT NULL,
    partial_refund_percentage DECIMAL(19, 2) NOT NULL
);

-- 4. Create refund_transactions table
CREATE TABLE refund_transactions (
    id UUID PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(id),
    adventure_booking_id BIGINT REFERENCES adventure_checkout_bookings(id),
    amount DECIMAL(19, 2) NOT NULL,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_reference VARCHAR(100),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Insert initial policies
INSERT INTO cancellation_policies (category, full_refund_limit_hours, partial_refund_limit_hours, partial_refund_percentage)
VALUES ('DEFAULT_ADVENTURE', 48, 24, 50.00);
