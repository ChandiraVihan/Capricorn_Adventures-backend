package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.*;
import com.capricorn_adventures.entity.*;
import com.capricorn_adventures.exception.BadRequestException;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureCheckoutBookingRepository;
import com.capricorn_adventures.repository.CarRentalBookingRepository;
import com.capricorn_adventures.service.CarRentalPartnerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarRentalServiceImplTest {

    @Mock private CarRentalBookingRepository carRentalBookingRepository;
    @Mock private AdventureCheckoutBookingRepository adventureCheckoutBookingRepository;
    @Mock private CarRentalPartnerClient partnerClient;
    @Mock private JavaMailSender mailSender;

    @InjectMocks private CarRentalServiceImpl service;

    // ── Shared fixtures ────────────────────────────────────────────────────

    private AdventureCheckoutBooking checkout;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "from", "noreply@capricornadventures.lk");

        Adventure adventure = new Adventure();
        adventure.setId(1L);
        adventure.setName("Yala Safari");
        adventure.setLocation("Yala National Park");

        AdventureSchedule schedule = new AdventureSchedule();
        schedule.setId(10L);

        checkout = new AdventureCheckoutBooking();
        checkout.setId(100L);
        checkout.setAdventure(adventure);
        checkout.setSchedule(schedule);
        checkout.setParticipants(2);
        checkout.setTotalPrice(new BigDecimal("15000.00"));
        checkout.setStatus(AdventureCheckoutStatus.PENDING);
        checkout.setBookingReference("ADV-2024-0001");

        User user = new User();
        ReflectionTestUtils.setField(user, "email", "test@example.com");
        ReflectionTestUtils.setField(user, "firstName", "Ashan");
        checkout.setUser(user);
    }

    // ── AC1 + AC2: Search returns vehicle list ─────────────────────────────

    @Test
    @DisplayName("AC2 – search returns available vehicles with price per day")
    void searchAvailableVehicles_returnsVehicles_whenPartnerResponds() {
        CarRentalVehicleDTO vehicle = buildVehicleDTO("V001", "Toyota Prado", "SUV",
                new BigDecimal("5000.00"), "LKR");

        when(partnerClient.searchAvailableVehicles(any(), any(), any(), any(), any()))
                .thenReturn(List.of(vehicle));

        CarRentalSearchResponseDTO response = service.searchAvailableVehicles(
                "Yala National Park",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(4),
                "SUV",
                "LKR");

        assertThat(response.isApiAvailable()).isTrue();
        assertThat(response.getVehicles()).hasSize(1);
        assertThat(response.getVehicles().get(0).getVehicleCategory()).isEqualTo("SUV");
        assertThat(response.getVehicles().get(0).getPricePerDay()).isEqualByComparingTo("5000.00");
    }

    // ── AC4: Graceful fallback ─────────────────────────────────────────────

    @Test
    @DisplayName("AC4 – returns fallback message with partner URL when API is unavailable")
    void searchAvailableVehicles_returnsFallback_whenPartnerReturnsEmpty() {
        when(partnerClient.searchAvailableVehicles(any(), any(), any(), any(), any()))
                .thenReturn(List.of()); // empty = unavailable
        when(partnerClient.getPartnerWebsiteUrl())
                .thenReturn("https://www.rentalpartner.example.com");

        CarRentalSearchResponseDTO response = service.searchAvailableVehicles(
                "Colombo", LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                null, "LKR");

        assertThat(response.isApiAvailable()).isFalse();
        assertThat(response.getFallbackPartnerUrl()).isNotBlank();
        assertThat(response.getFallbackMessage()).contains("temporarily unavailable");
        assertThat(response.getVehicles()).isNull();
    }

    @Test
    @DisplayName("AC4 – validation rejects returnDate <= pickupDate")
    void searchAvailableVehicles_throwsBadRequest_whenDatesInvalid() {
        LocalDate today = LocalDate.now();
        assertThatThrownBy(() ->
                service.searchAvailableVehicles("Colombo", today.plusDays(3), today.plusDays(1),
                        null, "LKR"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("returnDate must be after pickupDate");
    }

    // ── AC3: Add car to cart + combined total ──────────────────────────────

    @Test
    @DisplayName("AC3 – add car to cart returns combined adventure + car total")
    void addCarToCart_returnsCombinedTotal() {
        when(adventureCheckoutBookingRepository.findById(100L))
                .thenReturn(Optional.of(checkout));
        when(carRentalBookingRepository.findByAdventureCheckoutBookingId(100L))
                .thenReturn(Optional.empty());
        when(partnerClient.createReservation(any(), any(), any(), any()))
                .thenReturn("RESERVATION-XYZ");

        CarRentalVehicleDTO vehicle = buildVehicleDTO("V001", "Toyota Prado", "SUV",
                new BigDecimal("3000.00"), "LKR");
        vehicle.setTotalPrice(new BigDecimal("9000.00")); // 3 days
        when(partnerClient.searchAvailableVehicles(any(), any(), any(), any(), any()))
                .thenReturn(List.of(vehicle));

        when(carRentalBookingRepository.save(any(CarRentalBooking.class)))
                .thenAnswer(inv -> {
                    CarRentalBooking b = inv.getArgument(0);
                    ReflectionTestUtils.setField(b, "id", 999L);
                    return b;
                });

        CarRentalAddToCartRequestDTO request = buildAddToCartRequest(
                "V001", LocalDate.now().plusDays(1), LocalDate.now().plusDays(4), "LKR");

        CarRentalCartSummaryDTO summary = service.addCarToCart(request);

        // AC3: combined total = adventure (15 000) + car (9 000) = 24 000
        assertThat(summary.getAdventureTotal()).isEqualByComparingTo("15000.00");
        assertThat(summary.getCarRentalTotal()).isEqualByComparingTo("9000.00");
        assertThat(summary.getCombinedTotal()).isEqualByComparingTo("24000.00");
        assertThat(summary.getStatus()).isEqualTo("RESERVED");
    }

    @Test
    @DisplayName("AC3 – replaces existing RESERVED car when adding a new one")
    void addCarToCart_replacesExistingReservedCar() {
        CarRentalBooking existingRental = buildRentalBooking("OLD-RESERVATION", CarRentalStatus.RESERVED);

        when(adventureCheckoutBookingRepository.findById(100L))
                .thenReturn(Optional.of(checkout));
        when(carRentalBookingRepository.findByAdventureCheckoutBookingId(100L))
                .thenReturn(Optional.of(existingRental));
        when(partnerClient.createReservation(any(), any(), any(), any()))
                .thenReturn("NEW-RESERVATION");
        when(partnerClient.searchAvailableVehicles(any(), any(), any(), any(), any()))
                .thenReturn(List.of(buildVehicleDTO("V002", "Honda Vezel", "COMPACT",
                        new BigDecimal("2000.00"), "LKR")));
        when(carRentalBookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.addCarToCart(buildAddToCartRequest(
                "V002", LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), "LKR"));

        // Old reservation must be released
        verify(partnerClient).releaseReservation("OLD-RESERVATION");
        verify(carRentalBookingRepository).delete(existingRental);
    }

    // ── AC5: Remove car from cart ──────────────────────────────────────────

    @Test
    @DisplayName("AC5 – removing car releases partner reservation and recalculates total")
    void removeCarFromCart_releasesReservationAndRecalculatesTotal() {
        CarRentalBooking rental = buildRentalBooking("RES-001", CarRentalStatus.RESERVED);
        rental.setTotalPrice(new BigDecimal("9000.00"));

        when(adventureCheckoutBookingRepository.findById(100L))
                .thenReturn(Optional.of(checkout));
        when(carRentalBookingRepository.findByAdventureCheckoutBookingId(100L))
                .thenReturn(Optional.of(rental));
        when(partnerClient.releaseReservation("RES-001")).thenReturn(true);
        when(carRentalBookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CarRentalCartSummaryDTO summary = service.removeCarFromCart(100L);

        verify(partnerClient).releaseReservation("RES-001");

        // AC5: combined total recalculates back to adventure total only
        assertThat(summary.getCombinedTotal()).isEqualByComparingTo("15000.00");
        assertThat(summary.getCarRentalTotal()).isEqualByComparingTo("0");
        assertThat(summary.getStatus()).isEqualTo("RELEASED");
    }

    @Test
    @DisplayName("AC5 – still marks RELEASED even when partner release call fails")
    void removeCarFromCart_marksReleasedEvenIfPartnerFails() {
        CarRentalBooking rental = buildRentalBooking("RES-FAIL", CarRentalStatus.RESERVED);
        rental.setTotalPrice(new BigDecimal("6000.00"));

        when(adventureCheckoutBookingRepository.findById(100L))
                .thenReturn(Optional.of(checkout));
        when(carRentalBookingRepository.findByAdventureCheckoutBookingId(100L))
                .thenReturn(Optional.of(rental));
        when(partnerClient.releaseReservation("RES-FAIL")).thenReturn(false); // partner fails
        when(carRentalBookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CarRentalCartSummaryDTO summary = service.removeCarFromCart(100L);

        assertThat(summary.getStatus()).isEqualTo("RELEASED"); // local status still updated
    }

    @Test
    @DisplayName("AC5 – throws BadRequest when trying to remove a CONFIRMED rental")
    void removeCarFromCart_throwsBadRequest_whenAlreadyConfirmed() {
        CarRentalBooking rental = buildRentalBooking("RES-CONF", CarRentalStatus.CONFIRMED);

        when(adventureCheckoutBookingRepository.findById(100L))
                .thenReturn(Optional.of(checkout));
        when(carRentalBookingRepository.findByAdventureCheckoutBookingId(100L))
                .thenReturn(Optional.of(rental));

        assertThatThrownBy(() -> service.removeCarFromCart(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("CONFIRMED");
    }

    // ── AC6: Rental confirmation email ─────────────────────────────────────

    @Test
    @DisplayName("AC6 – confirms rental and sends separate rental email after payment")
    void confirmRentalAfterPayment_setsConfirmedAndSendsEmail() {
        CarRentalBooking rental = buildRentalBooking("RES-PAY", CarRentalStatus.RESERVED);

        when(carRentalBookingRepository.findByAdventureCheckoutBookingId(100L))
                .thenReturn(Optional.of(rental));
        when(carRentalBookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.confirmRentalAfterPayment(100L);

        verify(mailSender).send(any(SimpleMailMessage.class));
        assertThat(rental.getStatus()).isEqualTo(CarRentalStatus.CONFIRMED);
        assertThat(rental.isRentalConfirmationEmailSent()).isTrue();
    }

    @Test
    @DisplayName("AC6 – no email sent if no car add-on exists")
    void confirmRentalAfterPayment_doesNothing_whenNoCarAddon() {
        when(carRentalBookingRepository.findByAdventureCheckoutBookingId(100L))
                .thenReturn(Optional.empty());

        service.confirmRentalAfterPayment(100L);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // ── AC7: Currency matching ─────────────────────────────────────────────

    @Test
    @DisplayName("AC7 – search result currency matches requested preferredCurrency")
    void searchAvailableVehicles_returnsCurrencyMatchingPreference() {
        CarRentalVehicleDTO vehicleUSD = buildVehicleDTO("V003", "Nissan X-Trail", "SUV",
                new BigDecimal("16.13"), "USD"); // ~5000 LKR converted

        when(partnerClient.searchAvailableVehicles(any(), any(), any(), any(), eq("USD")))
                .thenReturn(List.of(vehicleUSD));

        CarRentalSearchResponseDTO response = service.searchAvailableVehicles(
                "Colombo", LocalDate.now().plusDays(1), LocalDate.now().plusDays(4),
                null, "USD");

        assertThat(response.getPreferredCurrency()).isEqualTo("USD");
        assertThat(response.getVehicles().get(0).getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("AC7 – cart summary currency matches rental's stored currency")
    void addCarToCart_cartSummaryCurrencyMatchesStoredCurrency() {
        when(adventureCheckoutBookingRepository.findById(100L))
                .thenReturn(Optional.of(checkout));
        when(carRentalBookingRepository.findByAdventureCheckoutBookingId(100L))
                .thenReturn(Optional.empty());
        when(partnerClient.createReservation(any(), any(), any(), any()))
                .thenReturn("RES-USD");

        CarRentalVehicleDTO vehicle = buildVehicleDTO("V003", "Nissan X-Trail", "SUV",
                new BigDecimal("16.00"), "USD");
        vehicle.setTotalPrice(new BigDecimal("48.00")); // 3 days
        when(partnerClient.searchAvailableVehicles(any(), any(), any(), any(), any()))
                .thenReturn(List.of(vehicle));
        when(carRentalBookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CarRentalAddToCartRequestDTO request = buildAddToCartRequest(
                "V003", LocalDate.now().plusDays(1), LocalDate.now().plusDays(4), "USD");

        CarRentalCartSummaryDTO summary = service.addCarToCart(request);

        assertThat(summary.getCurrency()).isEqualTo("USD");
    }

    // ── Edge cases ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getCartSummary returns null when no active rental exists")
    void getCartSummary_returnsNull_whenNoActiveRental() {
        when(adventureCheckoutBookingRepository.findById(100L))
                .thenReturn(Optional.of(checkout));
        when(carRentalBookingRepository.findByAdventureCheckoutBookingId(100L))
                .thenReturn(Optional.empty());

        CarRentalCartSummaryDTO result = service.getCartSummary(100L);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("addCarToCart throws ResourceNotFoundException for unknown checkout")
    void addCarToCart_throwsNotFound_whenCheckoutMissing() {
        when(adventureCheckoutBookingRepository.findById(999L))
                .thenReturn(Optional.empty());

        CarRentalAddToCartRequestDTO request = buildAddToCartRequest(
                "V001", LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), "LKR");
        request.setAdventureCheckoutBookingId(999L);

        assertThatThrownBy(() -> service.addCarToCart(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── Private builder helpers ────────────────────────────────────────────

    private CarRentalVehicleDTO buildVehicleDTO(String id, String name, String category,
                                                BigDecimal pricePerDay, String currency) {
        CarRentalVehicleDTO dto = new CarRentalVehicleDTO();
        dto.setPartnerVehicleId(id);
        dto.setVehicleName(name);
        dto.setVehicleCategory(category);
        dto.setVehicleImageUrl("https://cdn.example.com/" + id + ".jpg");
        dto.setPartnerName("RentalPartner");
        dto.setPartnerWebsiteUrl("https://www.rentalpartner.example.com");
        dto.setPricePerDay(pricePerDay);
        dto.setCurrency(currency);
        dto.setRentalDays(3);
        dto.setTotalPrice(pricePerDay.multiply(BigDecimal.valueOf(3)));
        return dto;
    }

    private CarRentalAddToCartRequestDTO buildAddToCartRequest(String vehicleId,
                                                               LocalDate pickup,
                                                               LocalDate returnDate,
                                                               String currency) {
        CarRentalAddToCartRequestDTO req = new CarRentalAddToCartRequestDTO();
        req.setAdventureCheckoutBookingId(100L);
        req.setPartnerVehicleId(vehicleId);
        req.setPartnerName("RentalPartner");
        req.setPickupDate(pickup);
        req.setReturnDate(returnDate);
        req.setPickupLocation("Yala National Park");
        req.setPreferredCurrency(currency);
        return req;
    }

    private CarRentalBooking buildRentalBooking(String reservationId, CarRentalStatus status) {
        CarRentalBooking rental = new CarRentalBooking();
        ReflectionTestUtils.setField(rental, "id", 50L);
        rental.setAdventureCheckoutBooking(checkout);
        rental.setPartnerReservationId(reservationId);
        rental.setPartnerName("RentalPartner");
        rental.setPartnerWebsiteUrl("https://www.rentalpartner.example.com");
        rental.setVehicleName("Toyota Prado");
        rental.setVehicleCategory("SUV");
        rental.setPickupDate(LocalDate.now().plusDays(1));
        rental.setReturnDate(LocalDate.now().plusDays(4));
        rental.setPickupLocation("Yala National Park");
        rental.setPricePerDay(new BigDecimal("3000.00"));
        rental.setTotalPrice(new BigDecimal("9000.00"));
        rental.setCurrency("LKR");
        rental.setStatus(status);
        return rental;
    }
}
