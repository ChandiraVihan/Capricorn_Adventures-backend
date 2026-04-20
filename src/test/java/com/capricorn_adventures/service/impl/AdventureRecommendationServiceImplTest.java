package com.capricorn_adventures.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.capricorn_adventures.dto.NearbyRecommendationDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.entity.AdventureCategory;
import com.capricorn_adventures.entity.AdventureCheckoutBooking;
import com.capricorn_adventures.entity.AdventureCheckoutStatus;
import com.capricorn_adventures.entity.AdventureSchedule;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureCheckoutBookingRepository;
import com.capricorn_adventures.repository.AdventureRepository;
import com.capricorn_adventures.service.DistanceMockService;
import com.capricorn_adventures.service.DistanceMockService.DistanceResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdventureRecommendationServiceImplTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private AdventureCheckoutBookingRepository bookingRepository;

    @Mock
    private DistanceMockService distanceMockService;

    private AdventureRecommendationServiceImpl recommendationService;

    private static final Double USER_LAT = 6.9271;
    private static final Double USER_LNG = 79.8612;

    @BeforeEach
    void setUp() {
        recommendationService = new AdventureRecommendationServiceImpl(
                adventureRepository, bookingRepository, distanceMockService);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Adventure adventure(long id, String location) {
        Adventure a = new Adventure();
        a.setId(id);
        a.setName("Adventure " + id);
        a.setDescription("Desc " + id);
        a.setBasePrice(BigDecimal.valueOf(1000));
        a.setLocation(location);
        a.setActive(true);
        AdventureCategory cat = new AdventureCategory();
        cat.setId(1L);
        cat.setName("Hiking");
        a.setCategory(cat);
        return a;
    }

    private DistanceResult distance(double km) {
        return new DistanceResult(km, (int) km + " mins");
    }

    private AdventureCheckoutBooking completedBooking(Adventure adventure) {
        AdventureCheckoutBooking b = new AdventureCheckoutBooking();
        b.setAdventure(adventure);
        b.setStatus(AdventureCheckoutStatus.CONFIRMED);
        AdventureSchedule s = new AdventureSchedule();
        s.setEndDate(LocalDateTime.now().minusDays(1));
        b.setSchedule(s);
        return b;
    }

    private AdventureCheckoutBooking pendingBooking(Adventure adventure) {
        AdventureCheckoutBooking b = new AdventureCheckoutBooking();
        b.setAdventure(adventure);
        b.setStatus(AdventureCheckoutStatus.PENDING);
        AdventureSchedule s = new AdventureSchedule();
        s.setEndDate(LocalDateTime.now().plusDays(10));
        b.setSchedule(s);
        return b;
    }

    // ── AC1: Homepage returns up to 6 adventures ranked by distance ────────────

    @Test
    void getAdventuresNearYou_returnsUpToSixAdventuresRankedByDistance() {
        List<Adventure> all = List.of(
                adventure(1, "Loc1"), adventure(2, "Loc2"), adventure(3, "Loc3"),
                adventure(4, "Loc4"), adventure(5, "Loc5"), adventure(6, "Loc6"),
                adventure(7, "Loc7"), adventure(8, "Loc8"), adventure(9, "Loc9"),
                adventure(10, "Loc10"));

        when(adventureRepository.findBrowseAdventures(null, null, null)).thenReturn(all);

        for (int i = 1; i <= 10; i++) {
            when(distanceMockService.calculateDistance(eq("Loc" + i), eq(USER_LAT), eq(USER_LNG), isNull()))
                    .thenReturn(distance(i * 5.0));
        }

        NearbyRecommendationDTO result = recommendationService.getAdventuresNearYou(USER_LAT, USER_LNG, null);

        assertEquals(6, result.getAdventuresNearYou().size());
        assertEquals(5.0, result.getAdventuresNearYou().get(0).getDistanceKm());
        assertEquals(30.0, result.getAdventuresNearYou().get(5).getDistanceKm());
    }

    // ── AC2: Completed adventures are excluded ─────────────────────────────────

    @Test
    void getAdventuresNearYou_excludesCompletedAdventuresForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        Adventure completed = adventure(1, "Loc1");
        Adventure available = adventure(2, "Loc2");

        when(adventureRepository.findBrowseAdventures(null, null, null))
                .thenReturn(List.of(completed, available));
        when(bookingRepository.findByUserIdWithDetailsOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(completedBooking(completed)));
        when(distanceMockService.calculateDistance(eq("Loc2"), eq(USER_LAT), eq(USER_LNG), isNull()))
                .thenReturn(distance(10.0));

        NearbyRecommendationDTO result = recommendationService.getAdventuresNearYou(USER_LAT, USER_LNG, userId);

        assertEquals(1, result.getAdventuresNearYou().size());
        assertEquals(2L, result.getAdventuresNearYou().get(0).getId());
    }

    @Test
    void getAdventuresNearYou_doesNotExcludePendingBookings() {
        UUID userId = UUID.randomUUID();
        Adventure pending = adventure(1, "Loc1");

        when(adventureRepository.findBrowseAdventures(null, null, null))
                .thenReturn(List.of(pending));
        when(bookingRepository.findByUserIdWithDetailsOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(pendingBooking(pending)));
        when(distanceMockService.calculateDistance(eq("Loc1"), eq(USER_LAT), eq(USER_LNG), isNull()))
                .thenReturn(distance(5.0));

        NearbyRecommendationDTO result = recommendationService.getAdventuresNearYou(USER_LAT, USER_LNG, userId);

        assertEquals(1, result.getAdventuresNearYou().size());
    }

    @Test
    void getAdventuresNearYou_whenNoUserId_doesNotQueryBookings() {
        when(adventureRepository.findBrowseAdventures(null, null, null)).thenReturn(List.of());

        recommendationService.getAdventuresNearYou(USER_LAT, USER_LNG, null);

        verifyNoInteractions(bookingRepository);
    }

    // ── AC3: More in This Area — 4 adventures within 20 km ────────────────────

    @Test
    void getMoreInThisArea_returnsFourAdventuresWithin20Km() {
        Adventure source = adventure(1, "SourceLoc");

        when(adventureRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(source));
        when(adventureRepository.findBrowseAdventures(null, null, null))
                .thenReturn(List.of(source,
                        adventure(2, "Loc2"), adventure(3, "Loc3"),
                        adventure(4, "Loc4"), adventure(5, "Loc5")));

        when(distanceMockService.calculateDistance(eq("Loc2"), isNull(), isNull(), eq("SourceLoc"))).thenReturn(distance(5.0));
        when(distanceMockService.calculateDistance(eq("Loc3"), isNull(), isNull(), eq("SourceLoc"))).thenReturn(distance(10.0));
        when(distanceMockService.calculateDistance(eq("Loc4"), isNull(), isNull(), eq("SourceLoc"))).thenReturn(distance(15.0));
        when(distanceMockService.calculateDistance(eq("Loc5"), isNull(), isNull(), eq("SourceLoc"))).thenReturn(distance(19.9));

        NearbyRecommendationDTO result = recommendationService.getMoreInThisArea(1L, USER_LAT, USER_LNG);

        assertEquals(4, result.getMoreInThisArea().size());
        assertEquals(20.0, result.getSearchRadiusKm());
        assertNull(result.getRadiusNote());
        assertTrue(result.getMoreInThisArea().stream().noneMatch(d -> d.getId().equals(1L)));
    }

    @Test
    void getMoreInThisArea_limitsResultsToFour() {
        Adventure source = adventure(1, "SourceLoc");

        when(adventureRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(source));
        when(adventureRepository.findBrowseAdventures(null, null, null))
                .thenReturn(List.of(source,
                        adventure(2, "Loc2"), adventure(3, "Loc3"), adventure(4, "Loc4"),
                        adventure(5, "Loc5"), adventure(6, "Loc6")));

        for (int i = 2; i <= 6; i++) {
            when(distanceMockService.calculateDistance(eq("Loc" + i), isNull(), isNull(), eq("SourceLoc")))
                    .thenReturn(distance(i * 2.0));
        }

        NearbyRecommendationDTO result = recommendationService.getMoreInThisArea(1L, USER_LAT, USER_LNG);

        assertEquals(4, result.getMoreInThisArea().size());
    }

    // ── AC4: Expand to 50 km when nothing found within 20 km ──────────────────

    @Test
    void getMoreInThisArea_expandsTo50KmWhenNothingWithin20Km() {
        Adventure source = adventure(1, "SourceLoc");
        Adventure farAdventure = adventure(2, "FarLoc");

        when(adventureRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(source));
        when(adventureRepository.findBrowseAdventures(null, null, null))
                .thenReturn(List.of(source, farAdventure));
        when(distanceMockService.calculateDistance(eq("FarLoc"), isNull(), isNull(), eq("SourceLoc")))
                .thenReturn(distance(35.0));

        NearbyRecommendationDTO result = recommendationService.getMoreInThisArea(1L, USER_LAT, USER_LNG);

        assertEquals(50.0, result.getSearchRadiusKm());
        assertNotNull(result.getRadiusNote());
        assertTrue(result.getRadiusNote().contains("50 km"));
        assertEquals(1, result.getMoreInThisArea().size());
        assertEquals(2L, result.getMoreInThisArea().get(0).getId());
    }

    @Test
    void getMoreInThisArea_doesNotExpandWhenExactly20Km() {
        Adventure source = adventure(1, "SourceLoc");
        Adventure edgeCase = adventure(2, "EdgeLoc");

        when(adventureRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(source));
        when(adventureRepository.findBrowseAdventures(null, null, null))
                .thenReturn(List.of(source, edgeCase));
        when(distanceMockService.calculateDistance(eq("EdgeLoc"), isNull(), isNull(), eq("SourceLoc")))
                .thenReturn(distance(20.0));

        NearbyRecommendationDTO result = recommendationService.getMoreInThisArea(1L, USER_LAT, USER_LNG);

        assertEquals(20.0, result.getSearchRadiusKm());
        assertNull(result.getRadiusNote());
        assertEquals(1, result.getMoreInThisArea().size());
    }

    @Test
    void getMoreInThisArea_throwsWhenAdventureNotFound() {
        when(adventureRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> recommendationService.getMoreInThisArea(999L, USER_LAT, USER_LNG));
    }

    @Test
    void getMoreInThisArea_excludesSourceAdventureFromResults() {
        Adventure source = adventure(1, "SourceLoc");

        when(adventureRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(source));
        when(adventureRepository.findBrowseAdventures(null, null, null))
                .thenReturn(List.of(source));

        NearbyRecommendationDTO result = recommendationService.getMoreInThisArea(1L, USER_LAT, USER_LNG);

        assertTrue(result.getMoreInThisArea().isEmpty());
    }
}
