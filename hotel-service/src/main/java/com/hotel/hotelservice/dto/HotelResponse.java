package com.hotel.hotelservice.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class HotelResponse {
    private Long id;
    private String name;
    private String address;
}