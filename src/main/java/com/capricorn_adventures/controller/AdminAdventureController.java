package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.CreateAdventureRequestDTO;
import com.capricorn_adventures.dto.CreateAdventureScheduleRequestDTO;
import com.capricorn_adventures.dto.UpdateAdventureRequestDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.entity.AdventureSchedule;
import com.capricorn_adventures.service.AdminAdventureService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/adventures")
@CrossOrigin(origins = "*")
public class AdminAdventureController {

    private final AdminAdventureService adminAdventureService;

    @Autowired
    public AdminAdventureController(AdminAdventureService adminAdventureService) {
        this.adminAdventureService = adminAdventureService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Adventure> createAdventure(@Valid @RequestBody CreateAdventureRequestDTO request) {
        Adventure created = adminAdventureService.createAdventure(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Adventure> updateAdventure(@PathVariable Long id, @Valid @RequestBody UpdateAdventureRequestDTO request) {
        Adventure updated = adminAdventureService.updateAdventure(id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdventureSchedule> createAdventureSchedule(@Valid @RequestBody CreateAdventureScheduleRequestDTO request) {
        AdventureSchedule created = adminAdventureService.createAdventureSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAdventure(@PathVariable Long id) {
        adminAdventureService.deleteAdventure(id);
        return ResponseEntity.noContent().build();
    }
}
