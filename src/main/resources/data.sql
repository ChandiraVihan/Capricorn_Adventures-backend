-- Add real room data here for sharing with group members
-- Format: INSERT INTO rooms (...) VALUES (...);

INSERT INTO rooms (id, name, description, base_price, max_occupancy) 
VALUES (999, 'Test Room', 'Test Room Description', 500.00, 2) 
ON CONFLICT (id) DO NOTHING;

-- BK-TEST-SUCCESS-001 (Needs Payment mapping it to SUCCESS)
INSERT INTO bookings (id, check_in_date, check_out_date, status, room_id, guest_name, guest_email, total_price, reference_id, is_refundable, refunded_amount)
VALUES (9991, '2026-05-01', '2026-05-05', 'CONFIRMED', 999, 'Test Guest', 'test@test.com', 2000.00, 'BK-TEST-SUCCESS-001', true, 0.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO payments (id, booking_id, transaction_id, amount, currency, status, gateway_method, created_at, updated_at)
VALUES (9991, 9991, 'TXN-001', 2000.00, 'USD', 'SUCCESS', 'PAYHERE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- BK-TEST-SUCCESS-003
INSERT INTO bookings (id, check_in_date, check_out_date, status, room_id, guest_name, guest_email, total_price, reference_id, is_refundable, refunded_amount)
VALUES (9993, '2026-05-01', '2026-05-05', 'CONFIRMED', 999, 'Test Guest', 'test@test.com', 2000.00, 'BK-TEST-SUCCESS-003', true, 0.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO payments (id, booking_id, transaction_id, amount, currency, status, gateway_method, created_at, updated_at)
VALUES (9993, 9993, 'TXN-003', 2000.00, 'USD', 'SUCCESS', 'PAYHERE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- BK-TEST-FAILED-002
INSERT INTO bookings (id, check_in_date, check_out_date, status, room_id, guest_name, guest_email, total_price, reference_id, is_refundable, refunded_amount)
VALUES (9992, '2026-05-01', '2026-05-05', 'CANCELLED', 999, 'Test Guest', 'test@test.com', 2000.00, 'BK-TEST-FAILED-002', true, 0.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO payments (id, booking_id, transaction_id, amount, currency, status, gateway_method, failure_reason, created_at, updated_at)
VALUES (9992, 9992, 'TXN-002', 2000.00, 'USD', 'FAILED', 'PAYHERE', 'Payment Failed', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- BK-TEST-CHARGEBACK-004
INSERT INTO bookings (id, check_in_date, check_out_date, status, room_id, guest_name, guest_email, total_price, reference_id, is_refundable, refunded_amount)
VALUES (9994, '2026-05-01', '2026-05-05', 'CANCELLED', 999, 'Test Guest', 'test@test.com', 2000.00, 'BK-TEST-CHARGEBACK-004', true, 0.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO payments (id, booking_id, transaction_id, amount, currency, status, gateway_method, failure_reason, created_at, updated_at)
VALUES (9994, 9994, 'TXN-004', 2000.00, 'USD', 'CHARGEBACK', 'PAYHERE', 'Chargeback initiated by cardholder', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
