package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.AmenityDTO;
import com.capricorn_adventures.dto.RoomDetailsDTO;
import com.capricorn_adventures.dto.RoomImageDTO;
import com.capricorn_adventures.entity.Amenity;
import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.entity.Room;
import com.capricorn_adventures.entity.RoomImage;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.exception.BadRequestException;
import com.capricorn_adventures.repository.BookingRepository;
import com.capricorn_adventures.repository.RoomRepository;
import com.capricorn_adventures.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public RoomDetailsDTO getRoomDetails(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));
        return mapToDTO(room);
    }

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new BadRequestException("Check-out date must be after check-in date!");
        }

        // Ensure the room exists first
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found with ID: " + roomId);
        }

        // CONFIRMED and PENDING bookings mean the room is occupied. 
        // CANCELLED or COMPLETED might mean it is available, depending on your exact business logic.
        List<BookingStatus> blockingStatuses = Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.PENDING);

        return !bookingRepository.isRoomOccupied(roomId, blockingStatuses, checkInDate, checkOutDate);
    }

    @Override
    public RoomDetailsDTO getRoomDetailsWithAvailability(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        RoomDetailsDTO roomDetails = getRoomDetails(roomId);
        // Additional availability logic can be appended to the DTO if you add a transient 'isAvailable' field,
        // or just rely on separate check. For now, we return the base details.
        return roomDetails;
    }

    @Override
    public List<RoomDetailsDTO> searchRooms(LocalDate checkInDate, LocalDate checkOutDate, Integer guests) {
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new BadRequestException("Check-out date must be after check-in date!");
        }

        List<BookingStatus> blockingStatuses = Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.PENDING);
        List<Room> rooms = roomRepository.findAvailableRooms(guests, blockingStatuses, checkInDate, checkOutDate);
        return rooms.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private RoomDetailsDTO mapToDTO(Room room) {
        RoomDetailsDTO dto = new RoomDetailsDTO();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setDescription(room.getDescription());
        dto.setBasePrice(room.getBasePrice());
        dto.setMaxOccupancy(room.getMaxOccupancy());

        if (room.getImages() != null) {
            List<RoomImageDTO> imageDTOs = room.getImages().stream().map(this::mapImageToDTO).collect(Collectors.toList());
            dto.setImages(imageDTOs);
        }

        if (room.getAmenities() != null) {
            List<AmenityDTO> amenityDTOs = room.getAmenities().stream().map(this::mapAmenityToDTO).collect(Collectors.toList());
            dto.setAmenities(amenityDTOs);
        }

        return dto;
    }

    private RoomImageDTO mapImageToDTO(RoomImage image) {
        RoomImageDTO dto = new RoomImageDTO();
        dto.setId(image.getId());
        dto.setImageUrl(image.getImageUrl());
        dto.setPrimary(image.isPrimary());
        return dto;
    }

    private AmenityDTO mapAmenityToDTO(Amenity amenity) {
        AmenityDTO dto = new AmenityDTO();
        dto.setId(amenity.getId());
        dto.setName(amenity.getName());
        dto.setIconIdentifier(amenity.getIconIdentifier());
        return dto;
    }
}
