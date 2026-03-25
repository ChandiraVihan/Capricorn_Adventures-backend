package com.capricorn_adventures.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.capricorn_adventures.dto.AdventureBrowseResponseDTO;
import com.capricorn_adventures.dto.AdventureDetailsResponseDTO;
import com.capricorn_adventures.dto.AdventureCategoryCardDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.entity.AdventureCategory;
import com.capricorn_adventures.entity.AdventureSchedule;
import com.capricorn_adventures.exception.BadRequestException;
import com.capricorn_adventures.exception.InvalidAdventureFilterException;
import com.capricorn_adventures.repository.AdventureCategoryCountProjection;
import com.capricorn_adventures.repository.AdventureCategoryRepository;
import com.capricorn_adventures.repository.AdventureRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdventureBrowseServiceImplTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private AdventureCategoryRepository adventureCategoryRepository;

    private AdventureBrowseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdventureBrowseServiceImpl(adventureRepository, adventureCategoryRepository);
    }

    @Test
    void getAdventureCategories_mapsCategoryCardsWithCounts() {
        when(adventureCategoryRepository.findActiveCategoriesWithAdventureCounts())
                .thenReturn(List.of(projection(1L, "Whale Watching", "thumb.jpg", 2L)));

        List<AdventureCategoryCardDTO> result = service.getAdventureCategories();

        assertEquals(1, result.size());
        assertEquals("Whale Watching", result.get(0).getName());
        assertEquals("thumb.jpg", result.get(0).getThumbnailUrl());
        assertEquals(2L, result.get(0).getAdventureCount());
    }

    @Test
    void browseAdventures_withEmptyResults_returnsNoAdventuresMessageAndSuggestions() {
        AdventureCategory category = new AdventureCategory();
        category.setId(1L);
        category.setName("Whale Watching");

        when(adventureCategoryRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(category));
        when(adventureRepository.findBrowseAdventures(1L, null, null)).thenReturn(List.of());
        when(adventureCategoryRepository.findSuggestedCategories(1L))
                .thenReturn(List.of(projection(2L, "Safari", "safari.jpg", 5L)));

        AdventureBrowseResponseDTO response = service.browseAdventures(1L, null, null, null, null, null);

        assertTrue(response.isEmptyState());
        assertEquals("No adventures available", response.getMessage());
        assertNotNull(response.getSuggestions());
        assertEquals(1, response.getSuggestions().size());
        assertEquals("Safari", response.getSuggestions().get(0).getName());
    }

    @Test
    void browseAdventures_withDurationRange_filtersAdventureList() {
        Adventure shortTrip = adventure("Short Trip", 2);
        Adventure mediumTrip = adventure("Medium Trip", 4);

        when(adventureRepository.findBrowseAdventures(null, null, null))
                .thenReturn(List.of(shortTrip, mediumTrip));

        AdventureBrowseResponseDTO response = service.browseAdventures(null, null, null, null, 3, 5);

        assertEquals(1, response.getAdventures().size());
        assertEquals("Medium Trip", response.getAdventures().get(0).getName());
        assertEquals(3, response.getAppliedFilters().getMinDurationHours());
        assertEquals(5, response.getAppliedFilters().getMaxDurationHours());
    }

    @Test
    void browseAdventures_withInvalidDurationRange_throwsException() {
        assertThrows(
                InvalidAdventureFilterException.class,
                () -> service.browseAdventures(null, null, null, null, 6, 3)
        );
    }

    @Test
    void browseAdventures_withCategoryName_resolvesCategoryAndQueriesById() {
        AdventureCategory category = new AdventureCategory();
        category.setId(7L);
        category.setName("Whale Watching");

        when(adventureCategoryRepository.findActiveByCategoryName("Whale Watching"))
                .thenReturn(Optional.of(category));
        when(adventureRepository.findBrowseAdventures(7L, BigDecimal.TEN, BigDecimal.valueOf(100)))
                .thenReturn(List.of(adventure("Blue Ocean", 3)));

        AdventureBrowseResponseDTO response = service.browseAdventures(
                null,
                "Whale Watching",
                BigDecimal.TEN,
                BigDecimal.valueOf(100),
                null,
                null
        );

        verify(adventureRepository).findBrowseAdventures(eq(7L), eq(BigDecimal.TEN), eq(BigDecimal.valueOf(100)));
        assertEquals(1, response.getAdventures().size());
        assertEquals("Blue Ocean", response.getAdventures().get(0).getName());
    }

    @Test
    void getAdventureDetails_returnsInactiveMessageWhenAdventureIsInactive() {
        Adventure adventure = adventure("Whale Explorer", 3);
        adventure.setActive(false);
        adventure.setDifficultyLevel("Moderate");
        adventure.setMinAge(12);

        when(adventureRepository.findByIdWithDetails(11L)).thenReturn(Optional.of(adventure));

        AdventureDetailsResponseDTO response = service.getAdventureDetails(11L, null, null);

        assertEquals("Moderate", response.getDifficultyLevel());
        assertEquals(12, response.getMinAge());
        assertEquals("This adventure is no longer bookable", response.getMessage());
        assertTrue(!response.isBookable());
    }

    @Test
    void getAdventureDetails_marksSlotsOutsideSelectedDatesAsDisabled() {
        Adventure adventure = adventure("Ocean Ride", 2);
        adventure.setId(21L);

        AdventureSchedule inRange = schedule(adventure, 2);
        inRange.setId(1L);
        inRange.setStartDate(LocalDateTime.of(2030, 1, 10, 9, 0));
        inRange.setEndDate(LocalDateTime.of(2030, 1, 10, 11, 0));

        AdventureSchedule outRange = schedule(adventure, 2);
        outRange.setId(2L);
        outRange.setStartDate(LocalDateTime.of(2030, 1, 15, 9, 0));
        outRange.setEndDate(LocalDateTime.of(2030, 1, 15, 11, 0));

        adventure.setSchedules(List.of(inRange, outRange));
        when(adventureRepository.findByIdWithDetails(21L)).thenReturn(Optional.of(adventure));

        AdventureDetailsResponseDTO response = service.getAdventureDetails(
                21L,
                LocalDate.of(2030, 1, 10),
                LocalDate.of(2030, 1, 12)
        );

        assertEquals(2, response.getScheduleSlots().size());
        assertTrue(response.getScheduleSlots().stream().anyMatch(s -> !s.isInSelectedRange() && s.isDisabled()));
    }

    @Test
    void validateAdventureBooking_throwsForUnderAgeGuest() {
        Adventure adventure = adventure("Safari", 3);
        adventure.setMinAge(16);
        adventure.setActive(true);
        when(adventureRepository.findByIdWithDetails(31L)).thenReturn(Optional.of(adventure));

        assertThrows(BadRequestException.class, () -> service.validateAdventureBooking(31L, 10, null));
    }

    @Test
    void validateAdventureBooking_throwsForInactiveAdventure() {
        Adventure adventure = adventure("Cultural Tour", 3);
        adventure.setActive(false);
        when(adventureRepository.findByIdWithDetails(41L)).thenReturn(Optional.of(adventure));

        assertThrows(BadRequestException.class, () -> service.validateAdventureBooking(41L, 20, null));
    }

    @Test
    void validateAdventureBooking_allowsWhenAgeAndScheduleAreValid() {
        Adventure adventure = adventure("Lagoon", 3);
        adventure.setId(51L);
        adventure.setMinAge(8);
        adventure.setActive(true);

        AdventureSchedule slot = schedule(adventure, 3);
        slot.setId(88L);
        slot.setStartDate(LocalDateTime.of(2030, 2, 1, 10, 0));
        slot.setEndDate(LocalDateTime.of(2030, 2, 1, 13, 0));
        adventure.setSchedules(List.of(slot));

        when(adventureRepository.findByIdWithDetails(51L)).thenReturn(Optional.of(adventure));

        assertTrue(service.validateAdventureBooking(51L, 22, 88L).isAllowed());
    }

    private Adventure adventure(String name, int durationHours) {
        Adventure adventure = new Adventure();
        adventure.setId((long) name.hashCode());
        adventure.setName(name);
        adventure.setDescription(name + " description");
        adventure.setBasePrice(BigDecimal.valueOf(100));
        adventure.setSchedules(List.of(schedule(adventure, durationHours)));

        AdventureCategory category = new AdventureCategory();
        category.setId(10L);
        category.setName("Water Sports");
        adventure.setCategory(category);
        return adventure;
    }

    private AdventureSchedule schedule(Adventure adventure, int durationHours) {
        AdventureSchedule schedule = new AdventureSchedule();
        schedule.setAdventure(adventure);
        schedule.setStartDate(LocalDateTime.of(2026, 1, 1, 9, 0));
        schedule.setEndDate(LocalDateTime.of(2026, 1, 1, 9 + durationHours, 0));
        schedule.setAvailableSlots(4);
        schedule.setStatus("AVAILABLE");
        return schedule;
    }

    private AdventureCategoryCountProjection projection(Long id, String name, String thumbnail, Long count) {
        return new AdventureCategoryCountProjection() {
            @Override
            public Long getCategoryId() {
                return id;
            }

            @Override
            public String getCategoryName() {
                return name;
            }

            @Override
            public String getThumbnailUrl() {
                return thumbnail;
            }

            @Override
            public Long getAdventureCount() {
                return count;
            }
        };
    }
}
