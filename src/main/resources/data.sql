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

-- Seed data for the manager operations dashboard and supporting adventure records.

INSERT INTO adventure_categories (id, name, thumbnail_url, is_active) VALUES
	(1, 'Water Adventures', 'https://images.example.com/adventure-categories/water.jpg', true),
	(2, 'Mountain Adventures', 'https://images.example.com/adventure-categories/mountain.jpg', true),
	(3, 'Wildlife Adventures', 'https://images.example.com/adventure-categories/wildlife.jpg', true);

INSERT INTO adventures (
	id,
	category_id,
	name,
	description,
	base_price,
	primary_image_url,
	location,
	difficulty_level,
	min_age,
	image_urls,
	is_active,
	itinerary,
	inclusions
) VALUES
	(
		1,
		1,
		'Madu River Kayak Safari',
		'Half-day guided kayak journey through mangroves and hidden channels.',
		12500.00,
		'https://images.example.com/adventures/madu-kayak/main.jpg',
		'Balapitiya',
		'EASY',
		10,
		'https://images.example.com/adventures/madu-kayak/1.jpg,https://images.example.com/adventures/madu-kayak/2.jpg',
		true,
		'Safety briefing, paddle training, guided mangrove trail, return transfer to base camp.',
		'Kayak, life jacket, guide support, bottled water'
	),
	(
		2,
		2,
		'Ella Sunrise Ridge Hike',
		'Early morning trek to panoramic viewpoints with local guide support.',
		9800.00,
		'https://images.example.com/adventures/ella-hike/main.jpg',
		'Ella',
		'MODERATE',
		12,
		'https://images.example.com/adventures/ella-hike/1.jpg,https://images.example.com/adventures/ella-hike/2.jpg',
		true,
		'Pre-dawn meetup, ridge ascent, sunrise viewpoint stop, tea break, descent.',
		'Guide fee, snack pack, first aid support'
	),
	(
		3,
		3,
		'Yala Jeep Safari Explorer',
		'Full-day wildlife drive focused on leopard zones and birding hotspots.',
		22000.00,
		'https://images.example.com/adventures/yala-safari/main.jpg',
		'Yala',
		'EASY',
		8,
		'https://images.example.com/adventures/yala-safari/1.jpg,https://images.example.com/adventures/yala-safari/2.jpg',
		true,
		'Park entry, morning drive, lunch break, evening drive, return to gate.',
		'4x4 vehicle, tracker, park permits, lunch'
	),
	(
		4,
		2,
		'Knuckles Overnight Trek',
		'Two-day highland trekking route with campsite stay and summit push.',
		28500.00,
		'https://images.example.com/adventures/knuckles-trek/main.jpg',
		'Knuckles Range',
		'HARD',
		16,
		'https://images.example.com/adventures/knuckles-trek/1.jpg,https://images.example.com/adventures/knuckles-trek/2.jpg',
		false,
		'Day 1 approach hike and campsite setup, Day 2 summit climb and descent.',
		'Guide team, tents, dinner and breakfast, safety kit'
	);

INSERT INTO adventure_schedules (
	id,
	adventure_id,
	start_date,
	end_date,
	available_slots,
	total_capacity,
	assigned_guide_name,
	checked_in_customer_count,
	status
) VALUES
	(1, 1, '2026-04-16 06:00:00', '2026-04-16 08:30:00', 2, 12, 'Nimal Perera', 7, 'AVAILABLE'),
	(2, 2, '2026-04-16 07:00:00', '2026-04-16 11:00:00', 0, 10, null, 6, 'AVAILABLE'),
	(3, 3, '2026-04-16 09:00:00', '2026-04-16 17:00:00', 8, 14, 'Sahan Rajapaksa', 4, 'AVAILABLE'),
	(4, 1, '2026-04-17 08:00:00', '2026-04-17 12:00:00', 9, 12, 'Ishara Fernando', 0, 'AVAILABLE'),
	(5, 2, '2026-04-17 05:30:00', '2026-04-17 10:00:00', 5, 10, null, 0, 'AVAILABLE'),
	(6, 3, '2026-04-18 06:30:00', '2026-04-18 16:30:00', 12, 14, 'Nimal Perera', 0, 'AVAILABLE'),
	(7, 4, '2026-04-19 07:00:00', '2026-04-20 16:00:00', 9, 9, 'Malinga Jayasuriya', 0, 'CANCELLED');

INSERT INTO operations_alerts (
	id,
	schedule_id,
	type,
	priority,
	title,
	message,
	resolved
) VALUES
	(1, 2, 'COMPLAINT', 'HIGH', 'Guest complaint about check-in delay', 'A customer complaint was raised for the Ella Sunrise Ridge Hike check-in queue.', false),
	(2, 3, 'COMPLAINT', 'MEDIUM', 'Guide requested clarification', 'The guide on the Yala Jeep Safari Explorer requested an updated participant list.', false);
