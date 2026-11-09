package com.hotel.hotelservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomRequest {

    @NotNull(message = "ID отеля обязателен")
    private Long hotelId;

    @NotBlank(message = "Номер комнаты обязателен")
    private String number;
}