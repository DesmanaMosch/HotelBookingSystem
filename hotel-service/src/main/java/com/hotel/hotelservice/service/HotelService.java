package com.hotel.hotelservice.service;

import com.hotel.hotelservice.dto.HotelRequest;
import com.hotel.hotelservice.dto.HotelResponse;
import com.hotel.hotelservice.entity.Hotel;
import com.hotel.hotelservice.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelService {

    private final HotelRepository hotelRepository;

    @Transactional
    public HotelResponse createHotel(HotelRequest request) {
        Hotel hotel = new Hotel();
        hotel.setName(request.getName());
        hotel.setAddress(request.getAddress());
        Hotel saved = hotelRepository.save(hotel);
        log.debug("Создан отель id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> getAllHotels() {
        return hotelRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private HotelResponse toResponse(Hotel hotel) {
        return new HotelResponse(hotel.getId(), hotel.getName(), hotel.getAddress());
    }
}