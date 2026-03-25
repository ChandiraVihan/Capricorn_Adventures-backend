package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.AdventureBrowseResponseDTO;
import com.capricorn_adventures.dto.AdventureCategoryCardDTO;
import java.math.BigDecimal;
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
}
