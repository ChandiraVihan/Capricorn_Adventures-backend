package com.capricorn_adventures.service;

import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.AdventureCheckoutBooking;
import com.capricorn_adventures.entity.CancellationPolicy;
import com.capricorn_adventures.repository.CancellationPolicyRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CancellationPolicyService {

    private final CancellationPolicyRepository policyRepository;

    public CancellationPolicyService(CancellationPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public BigDecimal calculateRefundAmount(Booking booking) {
        // Room booking policy: Full refund if > 48 hours before check-in.
        // Partial (50%) if between 24 and 48 hours.
        // No refund if < 24 hours.
        LocalDateTime checkIn = booking.getCheckInDate().atStartOfDay();
        long hoursBefore = Duration.between(LocalDateTime.now(), checkIn).toHours();

        if (hoursBefore >= 48) {
            return booking.getTotalPrice();
        } else if (hoursBefore >= 24) {
            return booking.getTotalPrice().multiply(new BigDecimal("0.5"));
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal calculateRefundAmount(AdventureCheckoutBooking booking) {
        // Adventure policy from database or default
        String category = booking.getAdventure().getCategory().getName();
        Optional<CancellationPolicy> policyOpt = policyRepository.findByCategory(category);
        
        if (policyOpt.isEmpty()) {
            // Default adventure policy: Full if > 48h, Partial (50%) if > 24h
            policyOpt = policyRepository.findByCategory("DEFAULT_ADVENTURE");
        }

        long hoursBefore = Duration.between(LocalDateTime.now(), booking.getSchedule().getStartDate()).toHours();

        if (policyOpt.isPresent()) {
            CancellationPolicy policy = policyOpt.get();
            if (hoursBefore >= policy.getFullRefundLimitHours()) {
                return booking.getTotalPrice();
            } else if (hoursBefore >= policy.getPartialRefundLimitHours()) {
                return booking.getTotalPrice().multiply(policy.getPartialRefundPercentage().divide(new BigDecimal("100")));
            }
        } else {
            // Hardcoded fallback if no policy found
            if (hoursBefore >= 48) return booking.getTotalPrice();
            if (hoursBefore >= 24) return booking.getTotalPrice().multiply(new BigDecimal("0.5"));
        }
        
        return BigDecimal.ZERO;
    }
}
