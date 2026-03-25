package com.capricorn_adventures.config;

import com.capricorn_adventures.User;
import com.capricorn_adventures.User.UserRole;
import com.capricorn_adventures.User.UserStatus;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.entity.Room;
import com.capricorn_adventures.repository.BookingRepository;
import com.capricorn_adventures.repository.RoomRepository;
import com.capricorn_adventures.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeData(UserRepository userRepository, 
                                           RoomRepository roomRepository,
                                           BookingRepository bookingRepository,
                                           PasswordEncoder passwordEncoder) {
        return args -> {
            // Only initialize if database is empty
            if (userRepository.count() == 0) {
                
                // Create test users
                User userA = new User();
                userA.setEmail("userA@example.com");
                userA.setPasswordHash(passwordEncoder.encode("password"));
                userA.setEmailVerified(true);
                userA.setStatus(UserStatus.ACTIVE);
                userA.setRole(UserRole.CUSTOMER);
                userRepository.save(userA);

                User userB = new User();
                userB.setEmail("userB@example.com");
                userB.setPasswordHash(passwordEncoder.encode("password"));
                userB.setEmailVerified(true);
                userB.setStatus(UserStatus.ACTIVE);
                userB.setRole(UserRole.CUSTOMER);
                userRepository.save(userB);

                User admin = new User();
                admin.setEmail("admin@example.com");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setEmailVerified(true);
                admin.setStatus(UserStatus.ACTIVE);
                admin.setRole(UserRole.ADMIN);
                userRepository.save(admin);

                // Create test rooms only when not already seeded via data.sql
                if (roomRepository.count() == 0) {
                    for (int i = 1; i <= 5; i++) {
                        Room room = new Room();
                        room.setName("Test Room " + i);
                        room.setDescription("Sample room for local development data");
                        room.setBasePrice(BigDecimal.valueOf(150 + (i * 25)));
                        room.setMaxOccupancy(2 + (i % 3));
                        roomRepository.save(room);
                    }
                }

                // Create bookings for User A (3 bookings across different months)
                List<Room> rooms = roomRepository.findAll();
                if (rooms.size() >= 3) {
                    Room room1 = rooms.get(0);
                    Room room2 = rooms.get(1);
                    Room room3 = rooms.get(2);

                    // Booking 1 - January 2026
                    Booking booking1 = new Booking();
                    // booking1.setReferenceId("BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    // booking1.setUser(userA);
                    booking1.setRoom(room1);
                    booking1.setCheckInDate(LocalDate.of(2026, 1, 15));
                    booking1.setCheckOutDate(LocalDate.of(2026, 1, 20));
                    booking1.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking1);

                    // Booking 2 - February 2026
                    Booking booking2 = new Booking();
                    // booking2.setReferenceId("BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    // booking2.setUser(userA);
                    booking2.setRoom(room2);
                    booking2.setCheckInDate(LocalDate.of(2026, 2, 10));
                    booking2.setCheckOutDate(LocalDate.of(2026, 2, 15));
                    booking2.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking2);

                    // Booking 3 - March 2026
                    Booking booking3 = new Booking();
                    // booking3.setReferenceId("BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    // booking3.setUser(userA);
                    booking3.setRoom(room3);
                    booking3.setCheckInDate(LocalDate.of(2026, 3, 1));
                    booking3.setCheckOutDate(LocalDate.of(2026, 3, 5));
                    booking3.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking3);

                    // Booking for User B - March 2026
                    Booking booking4 = new Booking();
                    // booking4.setReferenceId("BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    // booking4.setUser(userB);
                    booking4.setRoom(room1);
                    booking4.setCheckInDate(LocalDate.of(2026, 3, 10));
                    booking4.setCheckOutDate(LocalDate.of(2026, 3, 12));
                    booking4.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking4);
                } else {
                    System.out.println("! Skipping sample booking creation: at least 3 rooms are required");
                }

                System.out.println("✓ Test data initialized successfully!");
                System.out.println("  - User A: userA@example.com (3 bookings)");
                System.out.println("  - User B: userB@example.com (1 booking)");
                System.out.println("  - Admin: admin@example.com");
            }
        };
    }
}
