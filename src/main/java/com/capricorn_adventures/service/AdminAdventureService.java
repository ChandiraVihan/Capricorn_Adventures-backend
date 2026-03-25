package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.CreateAdventureRequestDTO;
import com.capricorn_adventures.dto.UpdateAdventureRequestDTO;
import com.capricorn_adventures.dto.CreateAdventureScheduleRequestDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.entity.AdventureSchedule;

public interface AdminAdventureService {
    Adventure createAdventure(CreateAdventureRequestDTO request);
    Adventure updateAdventure(Long id, UpdateAdventureRequestDTO request);
    AdventureSchedule createAdventureSchedule(CreateAdventureScheduleRequestDTO request);
}
