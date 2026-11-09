package com.hotel.hotelservice.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private Long hotelId;
    private String number;
    private boolean available;
    private int timesBooked;
}