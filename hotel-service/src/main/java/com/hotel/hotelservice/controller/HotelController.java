package com.hotel.hotelservice.controller;

import com.hotel.hotelservice.dto.HotelRequest;
import com.hotel.hotelservice.dto.HotelResponse;
import com.hotel.hotelservice.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HotelResponse createHotel(@Valid @RequestBody HotelRequest request) {
        return hotelService.createHotel(request);
    }

    @GetMapping
    public List<HotelResponse> getAllHotels() {
        return hotelService.getAllHotels();
    }
}