package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.AdventureCheckoutConfirmResponseDTO;
import com.capricorn_adventures.dto.AdventureCheckoutStartRequestDTO;
import com.capricorn_adventures.dto.AdventureCheckoutSummaryResponseDTO;
import com.capricorn_adventures.dto.GuestDetailsDTO;
import org.springframework.security.core.Authentication;

public interface AdventureCheckoutService {
    AdventureCheckoutSummaryResponseDTO startCheckout(AdventureCheckoutStartRequestDTO request,
                                                      Authentication authentication);

    AdventureCheckoutSummaryResponseDTO getCheckoutSummary(Long checkoutId);

    AdventureCheckoutSummaryResponseDTO updateGuest(Long checkoutId, GuestDetailsDTO guestDTO);

    AdventureCheckoutSummaryResponseDTO attachAuthenticatedUser(Long checkoutId, Authentication authentication);

    AdventureCheckoutConfirmResponseDTO confirmCheckout(Long checkoutId, boolean paymentSuccess);
}
