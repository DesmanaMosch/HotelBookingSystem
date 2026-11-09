package com.hotel.hotelservice.controller;

import com.hotel.hotelservice.dto.AvailabilityRequest;
import com.hotel.hotelservice.dto.RoomRequest;
import com.hotel.hotelservice.dto.RoomResponse;
import com.hotel.hotelservice.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse createRoom(@Valid @RequestBody RoomRequest request) {
        return roomService.createRoom(request);
    }

    @GetMapping
    public List<RoomResponse> getAllRooms() {
        return roomService.getAllAvailableRooms();
    }

    @GetMapping("/recommend")
    public List<RoomResponse> getRecommendedRooms() {
        return roomService.getRecommendedRooms();
    }

    @PostMapping("/{id}/confirm-availability")
    @ResponseStatus(HttpStatus.OK)
    public void confirmAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request) {
        roomService.confirmAvailability(id, request);
    }

    @PostMapping("/{id}/release")
    @ResponseStatus(HttpStatus.OK)
    public void releaseRoom(
            @PathVariable Long id,
            @RequestParam String requestId) {
        roomService.releaseRoom(id, requestId);
    }
}