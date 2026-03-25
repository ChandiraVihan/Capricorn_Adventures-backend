package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.AdventureCheckoutConfirmResponseDTO;
import com.capricorn_adventures.dto.AdventureCheckoutStartRequestDTO;
import com.capricorn_adventures.dto.AdventureCheckoutSummaryResponseDTO;
import com.capricorn_adventures.dto.GuestDetailsDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.entity.AdventureCheckoutBooking;
import com.capricorn_adventures.entity.AdventureCheckoutStatus;
import com.capricorn_adventures.entity.AdventureSchedule;
import com.capricorn_adventures.entity.User;
import com.capricorn_adventures.exception.BadRequestException;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureCheckoutBookingRepository;
import com.capricorn_adventures.repository.AdventureRepository;
import com.capricorn_adventures.repository.AdventureScheduleRepository;
import com.capricorn_adventures.repository.UserRepository;
import com.capricorn_adventures.service.AdventureCheckoutService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdventureCheckoutServiceImpl implements AdventureCheckoutService {

    private final AdventureRepository adventureRepository;
    private final AdventureScheduleRepository adventureScheduleRepository;
    private final AdventureCheckoutBookingRepository adventureCheckoutBookingRepository;
    private final UserRepository userRepository;

    @Autowired
    public AdventureCheckoutServiceImpl(AdventureRepository adventureRepository,
                                        AdventureScheduleRepository adventureScheduleRepository,
                                        AdventureCheckoutBookingRepository adventureCheckoutBookingRepository,
                                        UserRepository userRepository) {
        this.adventureRepository = adventureRepository;
        this.adventureScheduleRepository = adventureScheduleRepository;
        this.adventureCheckoutBookingRepository = adventureCheckoutBookingRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AdventureCheckoutSummaryResponseDTO startCheckout(AdventureCheckoutStartRequestDTO request,
                                                             Authentication authentication) {
        if (request.getParticipants() == null || request.getParticipants() < 1) {
            throw new BadRequestException("participants must be at least 1");
        }

        Adventure adventure = adventureRepository.findByIdWithDetails(request.getAdventureId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Adventure not found with ID: " + request.getAdventureId()));

        AdventureSchedule schedule = adventureScheduleRepository.findByIdWithAdventure(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with ID: " + request.getScheduleId()));

        validateSelection(adventure, schedule, request.getParticipants());

        AdventureCheckoutBooking booking = new AdventureCheckoutBooking();
        booking.setAdventure(adventure);
        booking.setSchedule(schedule);
        booking.setParticipants(request.getParticipants());
        booking.setUnitPrice(adventure.getBasePrice());
        booking.setTotalPrice(adventure.getBasePrice().multiply(BigDecimal.valueOf(request.getParticipants())));
        booking.setStatus(AdventureCheckoutStatus.PENDING);
        attachUserIfAuthenticated(booking, authentication);

        AdventureCheckoutBooking saved = adventureCheckoutBookingRepository.save(booking);
        return mapSummary(saved);
    }

    @Override
    public AdventureCheckoutSummaryResponseDTO getCheckoutSummary(Long checkoutId) {
        AdventureCheckoutBooking booking = getCheckoutOrThrow(checkoutId);
        return mapSummary(booking);
    }

    @Override
    @Transactional
    public AdventureCheckoutSummaryResponseDTO updateGuest(Long checkoutId, GuestDetailsDTO guestDTO) {
        AdventureCheckoutBooking booking = getCheckoutOrThrow(checkoutId);
        ensureEditable(booking);

        booking.setGuestName(guestDTO.getName());
        booking.setGuestEmail(guestDTO.getEmail());
        booking.setGuestPhone(guestDTO.getPhone());

        AdventureCheckoutBooking saved = adventureCheckoutBookingRepository.save(booking);
        return mapSummary(saved);
    }

    @Override
    @Transactional
    public AdventureCheckoutSummaryResponseDTO attachAuthenticatedUser(Long checkoutId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("Authentication is required to attach a user");
        }

        AdventureCheckoutBooking booking = getCheckoutOrThrow(checkoutId);
        ensureEditable(booking);

        UUID userId;
        try {
            userId = UUID.fromString(authentication.getName());
        } catch (Exception ex) {
            throw new BadRequestException("Authenticated user id is invalid");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        booking.setUser(user);

        AdventureCheckoutBooking saved = adventureCheckoutBookingRepository.save(booking);
        return mapSummary(saved);
    }

    @Override
    @Transactional
    public AdventureCheckoutConfirmResponseDTO confirmCheckout(Long checkoutId, boolean paymentSuccess) {
        AdventureCheckoutBooking booking = getCheckoutOrThrow(checkoutId);
        ensureEditable(booking);

        if (!paymentSuccess) {
            booking.setStatus(AdventureCheckoutStatus.PAYMENT_FAILED);
            booking.setPaymentFailureReason("Payment failed. Please retry or choose another payment method.");
            adventureCheckoutBookingRepository.save(booking);
            return paymentFailureResponse();
        }

        AdventureSchedule lockedSchedule = adventureScheduleRepository
                .findByIdForUpdateWithAdventure(booking.getSchedule().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with ID: " + booking.getSchedule().getId()));

        if (!lockedSchedule.getAdventure().getId().equals(booking.getAdventure().getId())) {
            throw new BadRequestException("Selected schedule does not belong to this adventure");
        }

        LocalDateTime now = LocalDateTime.now();
        if (lockedSchedule.getStartDate() == null || lockedSchedule.getStartDate().isBefore(now)
                || !"AVAILABLE".equalsIgnoreCase(lockedSchedule.getStatus())) {
            throw new BadRequestException("Selected time slot is no longer bookable. Please choose another slot.");
        }

        int availableSlots = lockedSchedule.getAvailableSlots() == null ? 0 : lockedSchedule.getAvailableSlots();
        if (availableSlots < booking.getParticipants()) {
            throw new BadRequestException("Selected time slot capacity has changed. Please choose another slot.");
        }

        int remaining = availableSlots - booking.getParticipants();
        lockedSchedule.setAvailableSlots(remaining);
        if (remaining == 0) {
            lockedSchedule.setStatus("SOLD_OUT");
        }

        Adventure adventure = booking.getAdventure();
        booking.setStatus(AdventureCheckoutStatus.CONFIRMED);
        booking.setBookingReference(generateReference());
        booking.setMeetingPoint(buildMeetingPoint(adventure));
        booking.setWhatToBring(adventure.getInclusions());
        booking.setPaymentFailureReason(null);

        adventureScheduleRepository.save(lockedSchedule);
        adventureCheckoutBookingRepository.save(booking);

        return paymentSuccessResponse(booking);
    }

    private AdventureCheckoutBooking getCheckoutOrThrow(Long checkoutId) {
        return adventureCheckoutBookingRepository.findByIdWithDetails(checkoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout not found with ID: " + checkoutId));
    }

    private void ensureEditable(AdventureCheckoutBooking booking) {
        if (booking.getStatus() == AdventureCheckoutStatus.CONFIRMED
                || booking.getStatus() == AdventureCheckoutStatus.CANCELLED) {
            throw new BadRequestException("This checkout can no longer be modified");
        }
    }

    private void validateSelection(Adventure adventure, AdventureSchedule schedule, int participants) {
        if (!adventure.isActive()) {
            throw new BadRequestException("This adventure is no longer bookable");
        }

        if (!schedule.getAdventure().getId().equals(adventure.getId())) {
            throw new BadRequestException("Selected schedule does not belong to this adventure");
        }

        LocalDateTime now = LocalDateTime.now();
        if (schedule.getStartDate() == null || schedule.getStartDate().isBefore(now)) {
            throw new BadRequestException("Selected time slot is in the past");
        }

        if (!"AVAILABLE".equalsIgnoreCase(schedule.getStatus())) {
            throw new BadRequestException("Selected time slot is not available");
        }

        int availableSlots = schedule.getAvailableSlots() == null ? 0 : schedule.getAvailableSlots();
        if (availableSlots < participants) {
            throw new BadRequestException("Selected time slot does not have enough capacity");
        }
    }

    private void attachUserIfAuthenticated(AdventureCheckoutBooking booking, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        try {
            UUID userId = UUID.fromString(authentication.getName());
            userRepository.findById(userId).ifPresent(booking::setUser);
        } catch (Exception ignored) {
            // Keep checkout as guest if authentication principal is not a UUID.
        }
    }

    private AdventureCheckoutSummaryResponseDTO mapSummary(AdventureCheckoutBooking booking) {
        AdventureCheckoutSummaryResponseDTO response = new AdventureCheckoutSummaryResponseDTO();
        response.setCheckoutId(booking.getId());
        response.setAdventureId(booking.getAdventure().getId());
        response.setAdventureName(booking.getAdventure().getName());
        response.setScheduleId(booking.getSchedule().getId());
        response.setBookingDate(booking.getSchedule().getStartDate() == null
                ? null
                : booking.getSchedule().getStartDate().toLocalDate());
        response.setStartTime(booking.getSchedule().getStartDate() == null
                ? null
                : booking.getSchedule().getStartDate().toLocalTime());
        response.setEndTime(booking.getSchedule().getEndDate() == null
                ? null
                : booking.getSchedule().getEndDate().toLocalTime());
        response.setParticipants(booking.getParticipants());
        response.setUnitPrice(booking.getUnitPrice());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus().name());
        response.setCanProceedAsGuest(true);
        response.setCanLoginOrRegister(true);
        response.setSelectionRetained(true);
        response.setAuthMessage("You can continue as guest or log in/register without losing your selection.");
        return response;
    }

    private AdventureCheckoutConfirmResponseDTO paymentFailureResponse() {
        AdventureCheckoutConfirmResponseDTO response = new AdventureCheckoutConfirmResponseDTO();
        response.setConfirmed(false);
        response.setRetryAllowed(true);
        response.setNextAction("RETRY_OR_CHANGE_PAYMENT");
        response.setMessage("Payment failed. Booking is not confirmed. Please retry or change payment method.");
        return response;
    }

    private AdventureCheckoutConfirmResponseDTO paymentSuccessResponse(AdventureCheckoutBooking booking) {
        AdventureCheckoutConfirmResponseDTO response = new AdventureCheckoutConfirmResponseDTO();
        response.setConfirmed(true);
        response.setBookingReference(booking.getBookingReference());
        response.setMeetingPoint(booking.getMeetingPoint());
        response.setWhatToBring(parseWhatToBring(booking.getWhatToBring()));
        response.setRetryAllowed(false);
        response.setNextAction("BOOKING_CONFIRMED");
        response.setMessage("Booking confirmed. Keep your reference for check-in instructions.");
        return response;
    }

    private String buildMeetingPoint(Adventure adventure) {
        if (adventure.getLocation() == null || adventure.getLocation().isBlank()) {
            return "Please refer to support for meeting point details.";
        }
        return adventure.getLocation().trim();
    }

    private List<String> parseWhatToBring(String inclusions) {
        if (inclusions == null || inclusions.isBlank()) {
            return List.of("Comfortable clothing", "Valid ID");
        }

        return Arrays.stream(inclusions.split(",|\\n"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
    }

    private String generateReference() {
        return "CAP-ADV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
