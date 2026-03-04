-- Insert Dummy Room
INSERT INTO rooms (id, name, description, base_price, max_occupancy) VALUES (1, 'Ocean View Suite', 'A beautiful suite overlooking the ocean with a king-sized bed.', 250.00, 2);

-- Insert Dummy Amenities
INSERT INTO amenities (id, name, icon_identifier) VALUES (1, 'Free Wi-Fi', 'wifi-icon');
INSERT INTO amenities (id, name, icon_identifier) VALUES (2, 'Air Conditioning', 'ac-icon');

-- Link Room and Amenities
INSERT INTO room_amenities (room_id, amenity_id) VALUES (1, 1);
INSERT INTO room_amenities (room_id, amenity_id) VALUES (1, 2);

-- Insert Room Image
INSERT INTO room_images (id, room_id, image_url, is_primary) VALUES (1, 1, 'https://example.com/ocean-suite-main.jpg', true);
