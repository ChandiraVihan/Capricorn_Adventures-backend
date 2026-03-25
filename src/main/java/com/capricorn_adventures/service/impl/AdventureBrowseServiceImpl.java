package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.AdventureBrowseResponseDTO;
import com.capricorn_adventures.dto.AdventureBookingValidationResponseDTO;
import com.capricorn_adventures.dto.AdventureCategoryCardDTO;
import com.capricorn_adventures.dto.AdventureDetailsResponseDTO;
import com.capricorn_adventures.dto.AdventureSummaryDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.entity.AdventureCategory;
import com.capricorn_adventures.entity.AdventureSchedule;
import com.capricorn_adventures.exception.BadRequestException;
import com.capricorn_adventures.exception.InvalidAdventureFilterException;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureCategoryCountProjection;
import com.capricorn_adventures.repository.AdventureCategoryRepository;
import com.capricorn_adventures.repository.AdventureRepository;
import com.capricorn_adventures.service.AdventureBrowseService;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

    @Override
    public AdventureDetailsResponseDTO getAdventureDetails(Long adventureId,
                                                           LocalDate selectedFromDate,
                                                           LocalDate selectedToDate) {
        validateSelectedDateRange(selectedFromDate, selectedToDate);

        Adventure adventure = adventureRepository.findByIdWithDetails(adventureId)
                .orElseThrow(() -> new ResourceNotFoundException("Adventure not found with ID: " + adventureId));

        List<AdventureDetailsResponseDTO.ScheduleSlotDTO> scheduleSlots = mapScheduleSlots(
                adventure,
                selectedFromDate,
                selectedToDate
        );

        boolean hasAvailableSlots = scheduleSlots.stream().anyMatch(AdventureDetailsResponseDTO.ScheduleSlotDTO::isAvailable);
        boolean bookable = adventure.isActive() && hasAvailableSlots;

        AdventureDetailsResponseDTO response = new AdventureDetailsResponseDTO();
        response.setId(adventure.getId());
        response.setName(adventure.getName());
        response.setDescription(adventure.getDescription());
        response.setBasePrice(adventure.getBasePrice());
        response.setPrimaryImageUrl(adventure.getPrimaryImageUrl());
        response.setPhotos(buildPhotoList(adventure));
        response.setLocation(adventure.getLocation());
        response.setDifficultyLevel(adventure.getDifficultyLevel());
        response.setMinAge(adventure.getMinAge());
        response.setItinerary(adventure.getItinerary());
        response.setInclusions(parseInclusions(adventure.getInclusions()));
        response.setActive(adventure.isActive());
        response.setBookable(bookable);
        response.setScheduleSlots(scheduleSlots);
        response.setMessage(buildDetailsMessage(adventure.isActive(), hasAvailableSlots, selectedFromDate, selectedToDate));
        return response;
    }

    @Override
    public AdventureBookingValidationResponseDTO validateAdventureBooking(Long adventureId,
                                                                          Integer age,
                                                                          Long scheduleId) {
        if (age == null || age < 0) {
            throw new BadRequestException("A valid age is required to book this adventure");
        }

        Adventure adventure = adventureRepository.findByIdWithDetails(adventureId)
                .orElseThrow(() -> new ResourceNotFoundException("Adventure not found with ID: " + adventureId));

        if (!adventure.isActive()) {
            throw new BadRequestException("This adventure is no longer bookable");
        }

        if (adventure.getMinAge() != null && age < adventure.getMinAge()) {
            throw new BadRequestException("Minimum age requirement is " + adventure.getMinAge());
        }

        AdventureSchedule targetSchedule = null;
        if (scheduleId != null) {
            targetSchedule = adventure.getSchedules() == null
                    ? null
                    : adventure.getSchedules().stream().filter(s -> scheduleId.equals(s.getId())).findFirst().orElse(null);
            if (targetSchedule == null) {
                throw new BadRequestException("Selected schedule does not belong to this adventure");
            }

            if (!isScheduleBookable(targetSchedule, true, LocalDateTime.now())) {
                throw new BadRequestException("Selected schedule is not available for booking");
            }
        }

        AdventureBookingValidationResponseDTO response = new AdventureBookingValidationResponseDTO();
        response.setAllowed(true);
        response.setMessage(targetSchedule == null
                ? "Eligible to book this adventure"
                : "Eligible to book the selected schedule");
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

    private void validateSelectedDateRange(LocalDate selectedFromDate, LocalDate selectedToDate) {
        if (selectedFromDate != null && selectedToDate != null && selectedToDate.isBefore(selectedFromDate)) {
            throw new InvalidAdventureFilterException("selectedToDate cannot be before selectedFromDate");
        }
    }

    private List<AdventureDetailsResponseDTO.ScheduleSlotDTO> mapScheduleSlots(Adventure adventure,
                                                                                LocalDate selectedFromDate,
                                                                                LocalDate selectedToDate) {
        if (adventure.getSchedules() == null) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();

        return adventure.getSchedules().stream()
                .filter(s -> s.getStartDate() != null)
                .filter(s -> !s.getStartDate().isBefore(now))
                .sorted(Comparator.comparing(AdventureSchedule::getStartDate))
                .map(s -> mapScheduleSlot(s, adventure.isActive(), selectedFromDate, selectedToDate, now))
                .collect(Collectors.toList());
    }

    private AdventureDetailsResponseDTO.ScheduleSlotDTO mapScheduleSlot(AdventureSchedule schedule,
                                                                        boolean adventureActive,
                                                                        LocalDate selectedFromDate,
                                                                        LocalDate selectedToDate,
                                                                        LocalDateTime now) {
        boolean inSelectedRange = isInSelectedRange(schedule, selectedFromDate, selectedToDate);
        boolean available = isScheduleBookable(schedule, adventureActive && inSelectedRange, now);

        AdventureDetailsResponseDTO.ScheduleSlotDTO dto = new AdventureDetailsResponseDTO.ScheduleSlotDTO();
        dto.setScheduleId(schedule.getId());
        dto.setStartDate(schedule.getStartDate());
        dto.setEndDate(schedule.getEndDate());
        dto.setAvailableSlots(schedule.getAvailableSlots());
        dto.setStatus(schedule.getStatus());
        dto.setInSelectedRange(inSelectedRange);
        dto.setAvailable(available);
        dto.setDisabled(!available);
        dto.setDisabledReason(resolveDisabledReason(schedule, adventureActive, inSelectedRange, now));
        return dto;
    }

    private boolean isInSelectedRange(AdventureSchedule schedule, LocalDate selectedFromDate, LocalDate selectedToDate) {
        if (selectedFromDate == null && selectedToDate == null) {
            return true;
        }

        LocalDate scheduleDate = schedule.getStartDate() == null ? null : schedule.getStartDate().toLocalDate();
        if (scheduleDate == null) {
            return false;
        }

        if (selectedFromDate != null && scheduleDate.isBefore(selectedFromDate)) {
            return false;
        }

        if (selectedToDate != null && scheduleDate.isAfter(selectedToDate)) {
            return false;
        }

        return true;
    }

    private boolean isScheduleBookable(AdventureSchedule schedule, boolean baseEligibility, LocalDateTime now) {
        if (!baseEligibility) {
            return false;
        }

        if (schedule.getStartDate() == null || schedule.getStartDate().isBefore(now)) {
            return false;
        }

        if (!"AVAILABLE".equalsIgnoreCase(schedule.getStatus())) {
            return false;
        }

        return schedule.getAvailableSlots() != null && schedule.getAvailableSlots() > 0;
    }

    private String resolveDisabledReason(AdventureSchedule schedule,
                                         boolean adventureActive,
                                         boolean inSelectedRange,
                                         LocalDateTime now) {
        if (!adventureActive) {
            return "ADVENTURE_INACTIVE";
        }
        if (!inSelectedRange) {
            return "NOT_IN_SELECTED_DATES";
        }
        if (schedule.getStartDate() == null || schedule.getStartDate().isBefore(now)) {
            return "PAST_DEPARTURE";
        }
        if (!"AVAILABLE".equalsIgnoreCase(schedule.getStatus())) {
            return "STATUS_UNAVAILABLE";
        }
        if (schedule.getAvailableSlots() == null || schedule.getAvailableSlots() <= 0) {
            return "NO_CAPACITY";
        }
        return null;
    }

    private List<String> buildPhotoList(Adventure adventure) {
        List<String> photos = new ArrayList<>();

        if (adventure.getPrimaryImageUrl() != null && !adventure.getPrimaryImageUrl().isBlank()) {
            photos.add(adventure.getPrimaryImageUrl().trim());
        }

        if (adventure.getImageUrls() != null && !adventure.getImageUrls().isBlank()) {
            Arrays.stream(adventure.getImageUrls().split(","))
                    .map(String::trim)
                    .filter(url -> !url.isBlank())
                    .filter(url -> !photos.contains(url))
                    .forEach(photos::add);
        }

        return photos;
    }

    private List<String> parseInclusions(String inclusions) {
        if (inclusions == null || inclusions.isBlank()) {
            return List.of();
        }

        return Arrays.stream(inclusions.split(",|\\n"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
    }

    private String buildDetailsMessage(boolean adventureActive,
                                       boolean hasAvailableSlots,
                                       LocalDate selectedFromDate,
                                       LocalDate selectedToDate) {
        if (!adventureActive) {
            return "This adventure is no longer bookable";
        }

        if (!hasAvailableSlots) {
            if (selectedFromDate != null || selectedToDate != null) {
                return "No schedules available for selected dates";
            }
            return "No upcoming schedules available";
        }

        return null;
    }
}
