# Booking Reference & History Management - Test Case Analysis
**Date:** March 4, 2026  
**Status:** IMPLEMENTATION REQUIRED

---

## Executive Summary
❌ **Current code CANNOT pass all test cases.** The implementation is incomplete and is missing critical features.

---

## Current Implementation Status

### ✅ What EXISTS
| Component | Status | Details |
|-----------|--------|---------|
| Booking Entity | Partial | Has `id`, `checkInDate`, `checkOutDate`, `status`, `room` |
| BookingService Interface | Partial | Only has `createBooking()` method |
| BookingServiceImpl | Partial | Creates bookings with room validation |
| BookingRepository | Partial | Only has `isRoomOccupied()` query |
| BookingController | Partial | Only has POST `/api/bookings` endpoint |
| User Entity | ✅ Complete | Has full user structure with UUID ID |

### ❌ What is MISSING / INCOMPLETE
| Component | Issue | Impact |
|-----------|-------|--------|
| **Booking Entity** | Missing `referenceId` field | ❌ API-TS-01, API-TS-02, API-TS-06 |
| **Booking Entity** | Missing `User` relationship (userId/user) | ❌ API-TS-04, API-TS-05, API-TS-07 |
| **Booking Entity** | Missing `bookingDate`/timestamp field | ❌ API-TS-05 (date filtering) |
| **BookingService** | Missing `findByReferenceId()` method | ❌ API-TS-02 |
| **BookingService** | Missing `getBookingHistory()` method | ❌ API-TS-05 |
| **BookingService** | Missing reference ID generation logic | ❌ API-TS-01, API-TS-06 |
| **BookingService** | Missing JWT authentication integration | ❌ API-TS-04, API-TS-05, API-TS-07 |
| **BookingRepository** | Missing `findByReferenceId()` query | ❌ API-TS-02 |
| **BookingRepository** | Missing `findByUserAndDateRange()` query | ❌ API-TS-05 |
| **BookingRepository** | Missing `findByUser()` for BOLA validation | ❌ API-TS-07 |
| **BookingController** | Missing `GET /api/bookings/ref/{referenceId}` | ❌ API-TS-02, API-TS-03 |
| **BookingController** | Missing `GET /api/bookings/history` | ❌ API-TS-05 |
| **BookingController** | Missing JWT auth on POST endpoint | ❌ API-TS-04 |
| **Exception Handling** | No 404 ResourceNotFoundException used | ❌ API-TS-03 |
| **Security** | No BOLA/IDOR prevention checks | ❌ API-TS-07 |

---

## Test Case vs Implementation Gap Analysis

### API-TS-01: Reference ID Generation
**Requirement:** POST /api/bookings generates unique, complex Reference ID  
**Current Status:** ❌ MISSING
- **Issue:** Booking entity has no `referenceId` field
- **Issue:** BookingService has no reference ID generation logic
- **What's needed:** Add UUID-based reference ID generation (e.g., "BK-" + 8-char random string)

### API-TS-02: Retrieve Booking by Reference ID
**Requirement:** GET /api/bookings/ref/{referenceId} returns booking  
**Current Status:** ❌ MISSING ENDPOINT
- **Issue:** No GET endpoint in BookingController
- **Issue:** No `findByReferenceId()` in BookingService
- **Issue:** No `findByReferenceId()` in BookingRepository
- **Issue:** No mapping from referenceId to response DTO

### API-TS-03: Invalid Reference ID Returns 404
**Requirement:** GET /api/bookings/ref/{invalidId} returns 404 with "Booking not found" message  
**Current Status:** ❌ NOT IMPLEMENTED
- **Issue:** No endpoint to test
- **Issue:** Exception handling not mapped to 404
- **Fix needed:** Use `ResourceNotFoundException` and proper @ExceptionHandler

### API-TS-04: Link Booking to Authenticated User
**Requirement:** POST /api/bookings + Bearer JWT automatically links booking to user  
**Current Status:** ❌ MISSING
- **Issue:** Booking entity has NO user/userId relationship
- **Issue:** BookingController does NOT accept JWT authentication
- **Issue:** No SecurityContext integration to get current user
- **Issue:** BookingService does not link user to booking

### API-TS-05: Booking History with Date Filtering
**Requirement:** GET /api/bookings/history?startDate=X&endDate=Y filters by authenticated user  
**Current Status:** ❌ MISSING ENDPOINT
- **Issue:** No GET /api/bookings/history endpoint
- **Issue:** No `getBookingHistory()` in BookingService
- **Issue:** No `findByUserAndDateRange()` in BookingRepository
- **Issue:** No JWT authentication on the endpoint
- **Issue:** Booking entity missing bookingDate/timestamp field for filtering
- **Issue:** Date parameters use LocalDate not LocalDateTime in entity

### API-TS-06: Reference ID Security (Non-Sequential)
**Requirement:** Reference IDs are cryptographically secure, not sequential  
**Current Status:** ❌ CANNOT VERIFY
- **Issue:** No implementation at all to verify
- **Fix needed:** Must use UUID.randomUUID() or SecureRandom for generation

### API-TS-07: BOLA/IDOR Prevention
**Requirement:** User B cannot access User A's booking history or retrieve User A's reference IDs  
**Current Status:** ❌ NOT IMPLEMENTED
- **Issue:** No user-booking relationship to check
- **Issue:** No authorization checks in BookingController
- **Issue:** No query filtering by current authenticated user
- **Issue:** No 403 Forbidden response for unauthorized access
- **Fix needed:** Add @PreAuthorize or manual authorization checks

---

## Required Code Changes

### 1. Booking Entity Enhancement
**File:** `src/main/java/com/capricorn_adventures/entity/Booking.java`

```java
// ADD:
@Column(unique = true, nullable = false)
private String referenceId;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;

@Column(nullable = false)
@CreationTimestamp
private LocalDateTime bookingDate;
```

### 2. BookingService Interface
**File:** `src/main/java/com/capricorn_adventures/service/BookingService.java`

```java
// ADD:
Booking findByReferenceId(String referenceId);
List<Booking> getBookingHistory(UUID userId, LocalDate startDate, LocalDate endDate);
```

### 3. BookingServiceImpl Implementation
**File:** `src/main/java/com/capricorn_adventures/service/impl/BookingServiceImpl.java`

```java
// ADD:
private String generateReferenceId() {
    return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
}

public Booking findByReferenceId(String referenceId) {
    return bookingRepository.findByReferenceId(referenceId)
        .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
}

public List<Booking> getBookingHistory(UUID userId, LocalDate startDate, LocalDate endDate) {
    return bookingRepository.findByUserIdAndCheckInDateBetween(userId, startDate, endDate);
}
```

### 4. BookingRepository Queries
**File:** `src/main/java/com/capricorn_adventures/repository/BookingRepository.java`

```java
// ADD:
Optional<Booking> findByReferenceId(String referenceId);

List<Booking> findByUserIdAndCheckInDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);
```

### 5. BookingController Endpoints
**File:** `src/main/java/com/capricorn_adventures/controller/BookingController.java`

```java
// MODIFY POST:
@PostMapping
public ResponseEntity<Booking> createBooking(
    @RequestBody BookingRequestDTO bookingRequestDTO,
    @AuthenticationPrincipal User currentUser) {
    // Link booking to current user
}

// ADD:
@GetMapping("/ref/{referenceId}")
public ResponseEntity<Booking> getBookingByReference(@PathVariable String referenceId) {
    // Return booking if found, throw 404 otherwise
}

@GetMapping("/history")
public ResponseEntity<List<Booking>> getBookingHistory(
    @RequestParam LocalDate startDate,
    @RequestParam LocalDate endDate,
    @AuthenticationPrincipal User currentUser) {
    // Return filtered bookings for current user only
}
```

### 6. BookingRequestDTO Enhancement
**File:** `src/main/java/com/capricorn_adventures/dto/BookingRequestDTO.java`

```java
// Verify it contains: roomId, checkInDate, checkOutDate
// User ID should come from JWT, NOT from request body
```

---

## Implementation Priority

| Priority | Test Cases | Component | Effort |
|----------|-----------|-----------|--------|
| 🔴 **CRITICAL** | API-TS-01, TS-04 | Add `referenceId` + `user` to Booking entity | 30 min |
| 🔴 **CRITICAL** | API-TS-04, TS-05, TS-07 | Add User-Booking relationship + JWT auth | 2 hours |
| 🔴 **CRITICAL** | API-TS-02, TS-03 | Add `GET /api/bookings/ref/{id}` endpoint | 1 hour |
| 🔴 **CRITICAL** | API-TS-05 | Add `GET /api/bookings/history` endpoint | 1.5 hours |
| 🟠 **HIGH** | API-TS-06 | Add secure UUID-based reference ID generation | 30 min |
| 🟠 **HIGH** | API-TS-07 | Add BOLA/IDOR authorization checks | 1 hour |
| 🟢 **MEDIUM** | Test Data | Set up database seeders for test data | 1 hour |

---

## Test Data Setup Required

```
Database Seed Requirements:
├── User A (userA@example.com)
│   ├── Booking 1 (Jan 2026)
│   ├── Booking 2 (Feb 2026)
│   └── Booking 3 (Mar 2026)
├── User B (userB@example.com)
│   └── Booking 1 (Mar 2026)
└── Rooms (at least 3 available for test dates)
```

---

## Next Steps

1. **Update Booking Entity** - Add referenceId, user relationship, bookingDate
2. **Update BookingService Interface & Implementation** - Add new methods
3. **Update BookingRepository** - Add required queries
4. **Update BookingController** - Add JWT auth, new endpoints
5. **Create BookingResponseDTO** - Include referenceId in responses
6. **Add Exception Handler** - Map ResourceNotFoundException to 404
7. **Add Security Configuration** - Ensure JWT validation on endpoints
8. **Create DataInitializer** - Seed test data
9. **Run Tests** - Verify all 7 test cases pass
10. **Load Test** - Verify reference IDs are truly unique under load

---

## Risk Assessment

| Risk | Severity | Mitigation |
|------|----------|-----------|
| Reference ID collision | Medium | Use UUID v4, add @Column(unique=true) constraint |
| Performance with history filtering | Low | Index on (user_id, checkInDate) |
| BOLA vulnerability if missed | High | Add explicit authorization checks on all user-specific endpoints |
| JWT validation inconsistency | Medium | Use centralized security config via SecurityConfig.java |

