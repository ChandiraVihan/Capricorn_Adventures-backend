package com.capricorn_adventures.service.impl;

// ═══════════════════════════════════════════════════════════════════════════════
// PATCH FILE – AdventureCheckoutServiceImpl.java
//
// This file shows the DIFF you need to apply to the existing
// AdventureCheckoutServiceImpl to wire in the car rental AC6 hook
// (send separate rental confirmation email after payment completes).
//
// Changes are marked with  // <<< ADD  and  // <<< END ADD
// ═══════════════════════════════════════════════════════════════════════════════

// ── Step 1: Add the new import at the top of the file ─────────────────────────
//
//   import com.capricorn_adventures.service.CarRentalService;
//
// ── Step 2: Add a constructor-injected field ──────────────────────────────────
//
//   private final CarRentalService carRentalService;           // <<< ADD
//
//   @Autowired
//   public AdventureCheckoutServiceImpl(AdventureRepository adventureRepository,
//                                       AdventureScheduleRepository adventureScheduleRepository,
//                                       AdventureCheckoutBookingRepository adventureCheckoutBookingRepository,
//                                       UserRepository userRepository,
//                                       CarRentalService carRentalService) {   // <<< ADD param
//       this.adventureRepository = adventureRepository;
//       this.adventureScheduleRepository = adventureScheduleRepository;
//       this.adventureCheckoutBookingRepository = adventureCheckoutBookingRepository;
//       this.userRepository = userRepository;
//       this.carRentalService = carRentalService;              // <<< ADD
//   }
//
// ── Step 3: Hook into confirmCheckout() ───────────────────────────────────────
//
// Locate the confirmCheckout method. After setting the booking status to
// PAYMENT_SUCCESS (or equivalent), add:
//
//   @Override
//   @Transactional
//   public AdventureCheckoutConfirmResponseDTO confirmCheckout(Long checkoutId,
//                                                              boolean paymentSuccess) {
//       AdventureCheckoutBooking booking = getCheckoutOrThrow(checkoutId);
//
//       if (paymentSuccess) {
//           booking.setStatus(AdventureCheckoutStatus.PAYMENT_SUCCESS);
//           adventureCheckoutBookingRepository.save(booking);
//
//           // AC6 – confirm car rental add-on and send separate rental email  // <<< ADD
//           carRentalService.confirmRentalAfterPayment(checkoutId);            // <<< ADD
//
//       } else {
//           booking.setStatus(AdventureCheckoutStatus.PAYMENT_FAILED);
//           adventureCheckoutBookingRepository.save(booking);
//       }
//
//       return mapConfirmResponse(booking);
//   }
//
// That single line is the only change needed in this file.
// ═══════════════════════════════════════════════════════════════════════════════

// No runnable code here – see comments above for the exact lines to add.
public class AdventureCheckoutServiceImplPatch {
    // intentionally empty – this class exists only to carry the patch instructions
    // as a peer source file. Delete this file after applying the changes above.
}
