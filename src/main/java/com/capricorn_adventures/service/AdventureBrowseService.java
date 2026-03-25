package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.AdventureBrowseResponseDTO;
import com.capricorn_adventures.dto.AdventureBookingValidationResponseDTO;
import com.capricorn_adventures.dto.AdventureCategoryCardDTO;
import com.capricorn_adventures.dto.AdventureDetailsResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface AdventureBrowseService {
    List<AdventureCategoryCardDTO> getAdventureCategories();

    AdventureCategoryCardDTO createAdventureCategory(String name, String thumbnailUrl);

    AdventureBrowseResponseDTO browseAdventures(Long categoryId,
                                                String category,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                Integer minDurationHours,
                                                Integer maxDurationHours);

    AdventureDetailsResponseDTO getAdventureDetails(Long adventureId,
                                                    LocalDate selectedFromDate,
                                                    LocalDate selectedToDate);

    AdventureBookingValidationResponseDTO validateAdventureBooking(Long adventureId,
                                                                   Integer age,
                                                                   Long scheduleId);
}
