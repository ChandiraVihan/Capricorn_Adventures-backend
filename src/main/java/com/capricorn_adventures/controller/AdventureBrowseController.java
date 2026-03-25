package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.AdventureBrowseResponseDTO;
import com.capricorn_adventures.dto.AdventureBrowseRequestDTO;
import com.capricorn_adventures.dto.AdventureCategoryCardDTO;
import com.capricorn_adventures.dto.CreateAdventureCategoryRequestDTO;
import com.capricorn_adventures.service.AdventureBrowseService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AdventureBrowseController {

    private final AdventureBrowseService adventureBrowseService;

    @Autowired
    public AdventureBrowseController(AdventureBrowseService adventureBrowseService) {
        this.adventureBrowseService = adventureBrowseService;
    }

    @GetMapping("/adventure-categories")
    public ResponseEntity<List<AdventureCategoryCardDTO>> getAdventureCategories() {
        return ResponseEntity.ok(adventureBrowseService.getAdventureCategories());
    }

    @PostMapping("/adventure-categories")
    public ResponseEntity<AdventureCategoryCardDTO> createAdventureCategory(
            @Valid @RequestBody CreateAdventureCategoryRequestDTO request) {
        AdventureCategoryCardDTO created = adventureBrowseService.createAdventureCategory(
                request.getName(),
                request.getThumbnailUrl()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/adventures")
    public ResponseEntity<AdventureBrowseResponseDTO> browseAdventures(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minDurationHours,
            @RequestParam(required = false) Integer maxDurationHours) {

        AdventureBrowseResponseDTO response = adventureBrowseService.browseAdventures(
                categoryId,
                category,
                minPrice,
                maxPrice,
                minDurationHours,
                maxDurationHours
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/adventures")
    public ResponseEntity<AdventureBrowseResponseDTO> browseAdventuresPost(
            @RequestBody(required = false) AdventureBrowseRequestDTO request) {

        if (request == null) {
            request = new AdventureBrowseRequestDTO();
        }

        AdventureBrowseResponseDTO response = adventureBrowseService.browseAdventures(
                request.getCategoryId(),
                request.getCategory(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getMinDurationHours(),
                request.getMaxDurationHours()
        );

        return ResponseEntity.ok(response);
    }
}
