package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.AdventureBrowseResponseDTO;
import com.capricorn_adventures.dto.AdventureCategoryCardDTO;
import com.capricorn_adventures.dto.AdventureSummaryDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.entity.AdventureCategory;
import com.capricorn_adventures.exception.InvalidAdventureFilterException;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureCategoryCountProjection;
import com.capricorn_adventures.repository.AdventureCategoryRepository;
import com.capricorn_adventures.repository.AdventureRepository;
import com.capricorn_adventures.service.AdventureBrowseService;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdventureBrowseServiceImpl implements AdventureBrowseService {

    private static final int SUGGESTION_LIMIT = 3;

    private final AdventureRepository adventureRepository;
    private final AdventureCategoryRepository adventureCategoryRepository;

    @Autowired
    public AdventureBrowseServiceImpl(AdventureRepository adventureRepository,
                                      AdventureCategoryRepository adventureCategoryRepository) {
        this.adventureRepository = adventureRepository;
        this.adventureCategoryRepository = adventureCategoryRepository;
    }

    @Override
    public List<AdventureCategoryCardDTO> getAdventureCategories() {
        List<AdventureCategoryCountProjection> categories = adventureCategoryRepository.findActiveCategoriesWithAdventureCounts();
        return categories.stream().map(this::mapCategoryProjection).collect(Collectors.toList());
    }

    @Override
    public AdventureCategoryCardDTO createAdventureCategory(String name, String thumbnailUrl) {
        if (name == null || name.isBlank()) {
            throw new InvalidAdventureFilterException("Category name is required");
        }

        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new InvalidAdventureFilterException("Category thumbnail URL is required");
        }

        String normalizedName = normalizeCategoryToken(name.trim());
        boolean duplicateExists = adventureCategoryRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(AdventureCategory::getName)
                .map(this::normalizeCategoryToken)
                .anyMatch(normalizedName::equals);

        if (duplicateExists) {
            throw new InvalidAdventureFilterException("Category already exists: " + name.trim());
        }

        AdventureCategory category = new AdventureCategory();
        category.setName(name.trim());
        category.setThumbnailUrl(thumbnailUrl.trim());
        category.setActive(true);

        AdventureCategory saved = adventureCategoryRepository.save(category);

        AdventureCategoryCardDTO dto = new AdventureCategoryCardDTO();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setThumbnailUrl(saved.getThumbnailUrl());
        dto.setAdventureCount(0L);
        return dto;
    }

    @Override
    public AdventureBrowseResponseDTO browseAdventures(Long categoryId,
                                                       String category,
                                                       BigDecimal minPrice,
                                                       BigDecimal maxPrice,
                                                       Integer minDurationHours,
                                                       Integer maxDurationHours) {
        validatePriceRange(minPrice, maxPrice);
        validateDurationRange(minDurationHours, maxDurationHours);

        Long resolvedCategoryId = resolveCategoryId(categoryId, category);

        List<Adventure> adventures = adventureRepository.findBrowseAdventures(resolvedCategoryId, minPrice, maxPrice);
        List<Adventure> filteredAdventures = applyDurationFilter(adventures, minDurationHours, maxDurationHours);

        AdventureBrowseResponseDTO response = new AdventureBrowseResponseDTO();
        response.setAdventures(filteredAdventures.stream().map(this::mapAdventure).collect(Collectors.toList()));
        response.setEmptyState(filteredAdventures.isEmpty());
        response.setAppliedFilters(buildAppliedFilters(
                resolvedCategoryId,
                category,
                minPrice,
                maxPrice,
                minDurationHours,
                maxDurationHours
        ));

        if (filteredAdventures.isEmpty()) {
            response.setMessage("No adventures available");
            response.setSuggestions(loadSuggestions(resolvedCategoryId));
        }

        return response;
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAdventureFilterException("minPrice cannot be negative");
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAdventureFilterException("maxPrice cannot be negative");
        }

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidAdventureFilterException("minPrice cannot be greater than maxPrice");
        }
    }

    private void validateDurationRange(Integer minDurationHours, Integer maxDurationHours) {
        if (minDurationHours != null && minDurationHours < 0) {
            throw new InvalidAdventureFilterException("minDurationHours cannot be negative");
        }

        if (maxDurationHours != null && maxDurationHours < 0) {
            throw new InvalidAdventureFilterException("maxDurationHours cannot be negative");
        }

        if (minDurationHours != null && maxDurationHours != null && minDurationHours > maxDurationHours) {
            throw new InvalidAdventureFilterException("minDurationHours cannot be greater than maxDurationHours");
        }
    }

    private List<Adventure> applyDurationFilter(List<Adventure> adventures,
                                                Integer minDurationHours,
                                                Integer maxDurationHours) {
        if (minDurationHours == null && maxDurationHours == null) {
            return adventures;
        }

        long minDurationMinutes = minDurationHours == null ? Long.MIN_VALUE : minDurationHours.longValue() * 60L;
        long maxDurationMinutes = maxDurationHours == null ? Long.MAX_VALUE : maxDurationHours.longValue() * 60L;

        return adventures.stream()
                .filter(adventure -> hasMatchingScheduleDuration(adventure, minDurationMinutes, maxDurationMinutes))
                .collect(Collectors.toList());
    }

    private boolean hasMatchingScheduleDuration(Adventure adventure,
                                                long minDurationMinutes,
                                                long maxDurationMinutes) {
        if (adventure.getSchedules() == null || adventure.getSchedules().isEmpty()) {
            return false;
        }

        return adventure.getSchedules().stream()
                .filter(schedule -> schedule.getStartDate() != null && schedule.getEndDate() != null)
                .filter(schedule -> "AVAILABLE".equalsIgnoreCase(schedule.getStatus()))
                .filter(schedule -> schedule.getAvailableSlots() != null && schedule.getAvailableSlots() > 0)
                .mapToLong(schedule -> Duration.between(schedule.getStartDate(), schedule.getEndDate()).toMinutes())
                .anyMatch(durationMinutes -> durationMinutes >= minDurationMinutes && durationMinutes <= maxDurationMinutes);
    }

    private Long resolveCategoryId(Long categoryId, String category) {
        if (categoryId != null) {
            boolean exists = adventureCategoryRepository.findByIdAndIsActiveTrue(categoryId).isPresent();
            if (!exists) {
                throw new ResourceNotFoundException("Category not found with ID: " + categoryId);
            }
            return categoryId;
        }

        if (category == null || category.isBlank()) {
            return null;
        }

        String trimmedCategory = category.trim();

        Optional<AdventureCategory> exactNameMatch = adventureCategoryRepository.findActiveByCategoryName(trimmedCategory);
        if (exactNameMatch.isPresent()) {
            return exactNameMatch.get().getId();
        }

        String normalizedCategory = normalizeCategoryToken(trimmedCategory);

        return adventureCategoryRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .filter(c -> normalizeCategoryToken(c.getName()).equals(normalizedCategory))
                .map(AdventureCategory::getId)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + category));
    }

    private List<AdventureCategoryCardDTO> loadSuggestions(Long excludedCategoryId) {
        return adventureCategoryRepository.findSuggestedCategories(excludedCategoryId)
                .stream()
                .limit(SUGGESTION_LIMIT)
                .map(this::mapCategoryProjection)
                .collect(Collectors.toList());
    }

    private AdventureSummaryDTO mapAdventure(Adventure adventure) {
        AdventureSummaryDTO dto = new AdventureSummaryDTO();
        dto.setId(adventure.getId());
        dto.setName(adventure.getName());
        dto.setDescription(adventure.getDescription());
        dto.setBasePrice(adventure.getBasePrice());
        dto.setPrimaryImageUrl(adventure.getPrimaryImageUrl());
        dto.setLocation(adventure.getLocation());

        if (adventure.getCategory() != null) {
            dto.setCategoryId(adventure.getCategory().getId());
            dto.setCategoryName(adventure.getCategory().getName());
        }

        return dto;
    }

    private AdventureCategoryCardDTO mapCategoryProjection(AdventureCategoryCountProjection projection) {
        AdventureCategoryCardDTO dto = new AdventureCategoryCardDTO();
        dto.setId(projection.getCategoryId());
        dto.setName(projection.getCategoryName());
        dto.setThumbnailUrl(projection.getThumbnailUrl());
        dto.setAdventureCount(projection.getAdventureCount());
        return dto;
    }

    private AdventureBrowseResponseDTO.AppliedFilters buildAppliedFilters(Long categoryId,
                                                                          String category,
                                                                          BigDecimal minPrice,
                                                                          BigDecimal maxPrice,
                                                                          Integer minDurationHours,
                                                                          Integer maxDurationHours) {
        AdventureBrowseResponseDTO.AppliedFilters appliedFilters = new AdventureBrowseResponseDTO.AppliedFilters();
        appliedFilters.setCategoryId(categoryId);
        appliedFilters.setCategory(category);
        appliedFilters.setMinPrice(minPrice);
        appliedFilters.setMaxPrice(maxPrice);
        appliedFilters.setMinDurationHours(minDurationHours);
        appliedFilters.setMaxDurationHours(maxDurationHours);
        return appliedFilters;
    }

    private String normalizeCategoryToken(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
