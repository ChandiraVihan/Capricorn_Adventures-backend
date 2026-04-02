package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.CreateAdventureRequestDTO;
import com.capricorn_adventures.dto.UpdateAdventureRequestDTO;
import com.capricorn_adventures.dto.CreateAdventureScheduleRequestDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.entity.AdventureCategory;
import com.capricorn_adventures.entity.AdventureSchedule;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureCategoryRepository;
import com.capricorn_adventures.repository.AdventureRepository;
import com.capricorn_adventures.repository.AdventureScheduleRepository;
import com.capricorn_adventures.repository.AdventureCheckoutBookingRepository;
import com.capricorn_adventures.service.AdminAdventureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAdventureServiceImpl implements AdminAdventureService {

    private final AdventureRepository adventureRepository;
    private final AdventureCategoryRepository categoryRepository;
    private final AdventureScheduleRepository scheduleRepository;
    private final AdventureCheckoutBookingRepository bookingRepository;

    @Autowired
    public AdminAdventureServiceImpl(AdventureRepository adventureRepository,
                                     AdventureCategoryRepository categoryRepository,
                                     AdventureScheduleRepository scheduleRepository,
                                     AdventureCheckoutBookingRepository bookingRepository) {
        this.adventureRepository = adventureRepository;
        this.categoryRepository = categoryRepository;
        this.scheduleRepository = scheduleRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public Adventure createAdventure(CreateAdventureRequestDTO request) {
        AdventureCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Adventure adventure = new Adventure();
        adventure.setCategory(category);
        adventure.setName(request.getName());
        adventure.setDescription(request.getDescription());
        adventure.setBasePrice(request.getBasePrice());
        adventure.setPrimaryImageUrl(request.getPrimaryImageUrl());
        adventure.setLocation(request.getLocation());
        adventure.setDifficultyLevel(request.getDifficultyLevel());
        adventure.setMinAge(request.getMinAge());
        adventure.setImageUrls(request.getImageUrls());
        adventure.setActive(request.isActive());
        adventure.setItinerary(request.getItinerary());
        adventure.setInclusions(request.getInclusions());

        return adventureRepository.save(adventure);
    }

    @Override
    @Transactional
    public Adventure updateAdventure(Long id, UpdateAdventureRequestDTO request) {
        Adventure adventure = adventureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adventure not found"));

        if (request.getCategoryId() != null) {
            AdventureCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            adventure.setCategory(category);
        }

        if (request.getName() != null) adventure.setName(request.getName());
        if (request.getDescription() != null) adventure.setDescription(request.getDescription());
        if (request.getBasePrice() != null) adventure.setBasePrice(request.getBasePrice());
        if (request.getPrimaryImageUrl() != null) adventure.setPrimaryImageUrl(request.getPrimaryImageUrl());
        if (request.getLocation() != null) adventure.setLocation(request.getLocation());
        if (request.getDifficultyLevel() != null) adventure.setDifficultyLevel(request.getDifficultyLevel());
        if (request.getMinAge() != null) adventure.setMinAge(request.getMinAge());
        if (request.getImageUrls() != null) adventure.setImageUrls(request.getImageUrls());
        if (request.getIsActive() != null) adventure.setActive(request.getIsActive());
        if (request.getItinerary() != null) adventure.setItinerary(request.getItinerary());
        if (request.getInclusions() != null) adventure.setInclusions(request.getInclusions());

        return adventureRepository.save(adventure);
    }

    @Override
    @Transactional
    public AdventureSchedule createAdventureSchedule(CreateAdventureScheduleRequestDTO request) {
        Adventure adventure = adventureRepository.findById(request.getAdventureId())
                .orElseThrow(() -> new ResourceNotFoundException("Adventure not found"));

        AdventureSchedule schedule = new AdventureSchedule();
        schedule.setAdventure(adventure);
        schedule.setStartDate(request.getStartDate());
        schedule.setEndDate(request.getEndDate());
        schedule.setAvailableSlots(request.getAvailableSlots());
        schedule.setStatus(request.getStatus() != null ? request.getStatus() : "AVAILABLE");

        return scheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public void deleteAdventure(Long id) {
        if (!adventureRepository.existsById(id)) {
            throw new ResourceNotFoundException("Adventure not found with id: " + id);
        }
        // First delete bookings associated with this adventure's schedules
        bookingRepository.deleteByAdventureId(id);
        
        // Then delete the adventure (schedules will be deleted by CascadeType.ALL)
        adventureRepository.deleteById(id);
    }
}
