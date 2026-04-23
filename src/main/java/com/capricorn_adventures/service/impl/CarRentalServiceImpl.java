package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.*;
import com.capricorn_adventures.entity.*;
import com.capricorn_adventures.exception.BadRequestException;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureCheckoutBookingRepository;
import com.capricorn_adventures.repository.CarRentalBookingRepository;
import com.capricorn_adventures.service.CarRentalPartnerClient;
import com.capricorn_adventures.service.CarRentalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class CarRentalServiceImpl implements CarRentalService {

    private static final Logger log = LoggerFactory.getLogger(CarRentalServiceImpl.class);

    private final CarRentalBookingRepository carRentalBookingRepository;
    private final AdventureCheckoutBookingRepository adventureCheckoutBookingRepository;
    private final CarRentalPartnerClient partnerClient;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    public CarRentalServiceImpl(CarRentalBookingRepository carRentalBookingRepository,
                                AdventureCheckoutBookingRepository adventureCheckoutBookingRepository,
                                CarRentalPartnerClient partnerClient,
                                JavaMailSender mailSender) {
        this.carRentalBookingRepository = carRentalBookingRepository;
        this.adventureCheckoutBookingRepository = adventureCheckoutBookingRepository;
        this.partnerClient = partnerClient;
        this.mailSender = mailSender;
    }

    // ── AC1 + AC2 + AC4 ───────────────────────────────────────────────────

    @Override
    public CarRentalSearchResponseDTO searchAvailableVehicles(String pickupLocation,
                                                              LocalDate pickupDate,
                                                              LocalDate returnDate,
                                                              String vehicleCategory,
                                                              String preferredCurrency) {
        if (returnDate.isBefore(pickupDate) || returnDate.isEqual(pickupDate)) {
            throw new BadRequestException("returnDate must be after pickupDate");
        }

        // AC7 – pass the user's preferred currency through to the partner client
        List<CarRentalVehicleDTO> vehicles = partnerClient.searchAvailableVehicles(
                pickupLocation, pickupDate, returnDate, vehicleCategory, preferredCurrency
        );

        if (vehicles.isEmpty()) {
            // AC4 – partner unavailable or no results: graceful fallback
            log.info("Car rental API returned no results for {}; returning fallback.", pickupLocation);
            return CarRentalSearchResponseDTO.fallback(partnerClient.getPartnerWebsiteUrl());
        }

        return CarRentalSearchResponseDTO.success(vehicles, pickupLocation, preferredCurrency);
    }

    // ── AC3: Add car to cart ───────────────────────────────────────────────

    @Override
    @Transactional
    public CarRentalCartSummaryDTO addCarToCart(CarRentalAddToCartRequestDTO request) {
        AdventureCheckoutBooking checkout = getCheckoutOrThrow(request.getAdventureCheckoutBookingId());

        // Prevent double-add: if RESERVED car already exists, replace it
        Optional<CarRentalBooking> existing =
                carRentalBookingRepository.findByAdventureCheckoutBookingId(checkout.getId());
        existing.ifPresent(old -> {
            if (old.getStatus() == CarRentalStatus.RESERVED) {
                // release the old reservation at the partner before replacing
                partnerClient.releaseReservation(old.getPartnerReservationId());
            }
            carRentalBookingRepository.delete(old);
        });

        // Create reservation at partner
        String partnerReservationId = partnerClient.createReservation(
                request.getPartnerVehicleId(),
                request.getPickupLocation(),
                request.getPickupDate(),
                request.getReturnDate()
        );

        if (partnerReservationId == null) {
            // Partner unavailable during reservation – use fallback reservation ID
            partnerReservationId = "PENDING-" + System.currentTimeMillis();
            log.warn("Partner reservation failed; using local placeholder ID {}", partnerReservationId);
        }

        // Fetch vehicle details from partner to get price (re-use search for simplicity)
        List<CarRentalVehicleDTO> results = partnerClient.searchAvailableVehicles(
                request.getPickupLocation(),
                request.getPickupDate(),
                request.getReturnDate(),
                null,
                request.getPreferredCurrency()
        );

        CarRentalVehicleDTO selectedVehicle = results.stream()
                .filter(v -> v.getPartnerVehicleId().equals(request.getPartnerVehicleId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle not found in current availability: " + request.getPartnerVehicleId()));

        // Persist the rental booking
        CarRentalBooking rental = new CarRentalBooking();
        rental.setAdventureCheckoutBooking(checkout);
        rental.setPartnerReservationId(partnerReservationId);
        rental.setPartnerName(request.getPartnerName());
        rental.setPartnerWebsiteUrl(partnerClient.getPartnerWebsiteUrl());
        rental.setVehicleName(selectedVehicle.getVehicleName());
        rental.setVehicleCategory(selectedVehicle.getVehicleCategory());
        rental.setVehicleImageUrl(selectedVehicle.getVehicleImageUrl());
        rental.setPickupDate(request.getPickupDate());
        rental.setReturnDate(request.getReturnDate());
        rental.setPickupLocation(request.getPickupLocation());
        rental.setPricePerDay(selectedVehicle.getPricePerDay());
        rental.setTotalPrice(selectedVehicle.getTotalPrice());
        rental.setCurrency(request.getPreferredCurrency());
        rental.setStatus(CarRentalStatus.RESERVED);

        CarRentalBooking saved = carRentalBookingRepository.save(rental);
        log.info("Car rental RESERVED: checkout={}, reservation={}", checkout.getId(), partnerReservationId);

        return buildCartSummary(saved, checkout.getTotalPrice());
    }

    // ── AC5: Remove car from cart ──────────────────────────────────────────

    @Override
    @Transactional
    public CarRentalCartSummaryDTO removeCarFromCart(Long adventureCheckoutBookingId) {
        AdventureCheckoutBooking checkout = getCheckoutOrThrow(adventureCheckoutBookingId);

        CarRentalBooking rental = carRentalBookingRepository
                .findByAdventureCheckoutBookingId(adventureCheckoutBookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No car rental add-on found for checkout " + adventureCheckoutBookingId));

        if (rental.getStatus() != CarRentalStatus.RESERVED) {
            throw new BadRequestException(
                    "Cannot remove a car rental that is already " + rental.getStatus());
        }

        // AC5 – release reservation at the partner
        boolean released = partnerClient.releaseReservation(rental.getPartnerReservationId());
        if (!released) {
            log.warn("Partner could not release reservation {}; marking RELEASED locally anyway.",
                    rental.getPartnerReservationId());
        }

        rental.setStatus(CarRentalStatus.RELEASED);
        CarRentalBooking saved = carRentalBookingRepository.save(rental);

        log.info("Car rental RELEASED: checkout={}, reservation={}",
                adventureCheckoutBookingId, rental.getPartnerReservationId());

        // AC5 – combined total recalculates (car total is excluded)
        return buildCartSummaryAfterRemoval(saved, checkout.getTotalPrice());
    }

    // ── AC6: Confirm rental after payment ─────────────────────────────────

    @Override
    @Transactional
    public void confirmRentalAfterPayment(Long adventureCheckoutBookingId) {
        Optional<CarRentalBooking> rentalOpt =
                carRentalBookingRepository.findByAdventureCheckoutBookingId(adventureCheckoutBookingId);

        if (rentalOpt.isEmpty()) {
            // No car add-on – nothing to do
            return;
        }

        CarRentalBooking rental = rentalOpt.get();

        if (rental.getStatus() != CarRentalStatus.RESERVED) {
            log.warn("Attempted to confirm rental {} with unexpected status {}",
                    rental.getId(), rental.getStatus());
            return;
        }

        rental.setStatus(CarRentalStatus.CONFIRMED);

        // AC6 – send separate rental confirmation email
        sendRentalConfirmationEmail(rental);
        rental.setRentalConfirmationEmailSent(true);

        carRentalBookingRepository.save(rental);
        log.info("Car rental CONFIRMED for checkout {}", adventureCheckoutBookingId);
    }

    // ── Get current cart summary ───────────────────────────────────────────

    @Override
    public CarRentalCartSummaryDTO getCartSummary(Long adventureCheckoutBookingId) {
        AdventureCheckoutBooking checkout = getCheckoutOrThrow(adventureCheckoutBookingId);

        return carRentalBookingRepository
                .findByAdventureCheckoutBookingId(adventureCheckoutBookingId)
                .filter(r -> r.getStatus() == CarRentalStatus.RESERVED
                        || r.getStatus() == CarRentalStatus.CONFIRMED)
                .map(r -> buildCartSummary(r, checkout.getTotalPrice()))
                .orElse(null);
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private AdventureCheckoutBooking getCheckoutOrThrow(Long id) {
        return adventureCheckoutBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Adventure checkout booking not found: " + id));
    }

    /**
     * AC3 / AC7 – build cart summary including combined total.
     */
    private CarRentalCartSummaryDTO buildCartSummary(CarRentalBooking rental,
                                                     BigDecimal adventureTotal) {
        CarRentalCartSummaryDTO dto = new CarRentalCartSummaryDTO();
        dto.setCarRentalBookingId(rental.getId());
        dto.setStatus(rental.getStatus().name());
        dto.setVehicleName(rental.getVehicleName());
        dto.setVehicleCategory(rental.getVehicleCategory());
        dto.setVehicleImageUrl(rental.getVehicleImageUrl());
        dto.setPartnerName(rental.getPartnerName());
        dto.setPartnerWebsiteUrl(rental.getPartnerWebsiteUrl());
        dto.setPickupDate(rental.getPickupDate().toString());
        dto.setReturnDate(rental.getReturnDate().toString());
        dto.setPickupLocation(rental.getPickupLocation());

        int rentalDays = (int) ChronoUnit.DAYS.between(rental.getPickupDate(), rental.getReturnDate());
        dto.setRentalDays(rentalDays);
        dto.setPricePerDay(rental.getPricePerDay());
        dto.setCarRentalTotal(rental.getTotalPrice());
        dto.setCurrency(rental.getCurrency());                  // AC7

        dto.setAdventureTotal(adventureTotal);
        dto.setCombinedTotal(adventureTotal.add(rental.getTotalPrice())); // AC3
        return dto;
    }

    /**
     * AC5 – after removal the combined total equals just the adventure total.
     */
    private CarRentalCartSummaryDTO buildCartSummaryAfterRemoval(CarRentalBooking rental,
                                                                 BigDecimal adventureTotal) {
        CarRentalCartSummaryDTO dto = buildCartSummary(rental, adventureTotal);
        // Override combined total: car is removed so it no longer counts
        dto.setCombinedTotal(adventureTotal);
        dto.setCarRentalTotal(BigDecimal.ZERO);
        return dto;
    }

    /**
     * AC6 – send a separate rental confirmation email alongside the adventure email.
     */
    private void sendRentalConfirmationEmail(CarRentalBooking rental) {
        AdventureCheckoutBooking checkout = rental.getAdventureCheckoutBooking();

        String toEmail;
        String firstName;

        if (checkout.getUser() != null && checkout.getUser().getEmail() != null) {
            toEmail    = checkout.getUser().getEmail();
            firstName  = checkout.getUser().getFirstName();
        } else if (checkout.getGuestEmail() != null) {
            toEmail   = checkout.getGuestEmail();
            firstName = checkout.getGuestName() != null ? checkout.getGuestName() : "Valued Customer";
        } else {
            log.warn("Cannot send rental confirmation: no email for checkout {}", checkout.getId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("Car Rental Confirmation – " + checkout.getBookingReference());
        message.setText(
                "Hi " + firstName + ",\n\n" +
                        "Your car rental add-on has been confirmed!\n\n" +
                        "Rental Details:\n" +
                        "  Partner:         " + rental.getPartnerName() + "\n" +
                        "  Vehicle:         " + rental.getVehicleName()
                        + " (" + rental.getVehicleCategory() + ")\n" +
                        "  Pickup Location: " + rental.getPickupLocation() + "\n" +
                        "  Pickup Date:     " + rental.getPickupDate() + "\n" +
                        "  Return Date:     " + rental.getReturnDate() + "\n" +
                        "  Total Price:     " + rental.getCurrency() + " " + rental.getTotalPrice() + "\n\n" +
                        "Your adventure booking reference: " + checkout.getBookingReference() + "\n\n" +
                        "Thank you for choosing Capricorn Adventures!\n\n" +
                        "— Capricorn Adventures Team"
        );

        try {
            mailSender.send(message);
            log.info("Rental confirmation email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send rental confirmation email to {}", toEmail, e);
        }
    }
}
