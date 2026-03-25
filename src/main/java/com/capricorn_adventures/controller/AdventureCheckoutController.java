package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.AdventureCheckoutConfirmResponseDTO;
import com.capricorn_adventures.dto.AdventureCheckoutStartRequestDTO;
import com.capricorn_adventures.dto.AdventureCheckoutSummaryResponseDTO;
import com.capricorn_adventures.dto.GuestDetailsDTO;
import com.capricorn_adventures.service.AdventureCheckoutService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/adventure-checkout")
@CrossOrigin(origins = "*")
public class AdventureCheckoutController {

    private final AdventureCheckoutService adventureCheckoutService;

    @Autowired
    public AdventureCheckoutController(AdventureCheckoutService adventureCheckoutService) {
        this.adventureCheckoutService = adventureCheckoutService;
    }

    @PostMapping("/start")
    public ResponseEntity<AdventureCheckoutSummaryResponseDTO> startCheckout(
            @Valid @RequestBody AdventureCheckoutStartRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(adventureCheckoutService.startCheckout(request, authentication));
    }

    @GetMapping("/{checkoutId}")
    public ResponseEntity<AdventureCheckoutSummaryResponseDTO> getCheckoutSummary(@PathVariable Long checkoutId) {
        return ResponseEntity.ok(adventureCheckoutService.getCheckoutSummary(checkoutId));
    }

    @PutMapping("/{checkoutId}/guest")
    public ResponseEntity<AdventureCheckoutSummaryResponseDTO> updateGuest(
            @PathVariable Long checkoutId,
            @Valid @RequestBody GuestDetailsDTO guestDTO) {
        return ResponseEntity.ok(adventureCheckoutService.updateGuest(checkoutId, guestDTO));
    }

    @PostMapping("/{checkoutId}/attach-user")
    public ResponseEntity<AdventureCheckoutSummaryResponseDTO> attachAuthenticatedUser(
            @PathVariable Long checkoutId,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(adventureCheckoutService.attachAuthenticatedUser(checkoutId, authentication));
    }

    @PostMapping("/{checkoutId}/confirm")
    public ResponseEntity<AdventureCheckoutConfirmResponseDTO> confirmCheckout(
            @PathVariable Long checkoutId,
            @RequestParam boolean paymentSuccess) {
        return ResponseEntity.ok(adventureCheckoutService.confirmCheckout(checkoutId, paymentSuccess));
    }
}
