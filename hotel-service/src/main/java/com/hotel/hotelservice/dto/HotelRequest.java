package com.hotel.hotelservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HotelRequest {

    @NotBlank(message = "Название отеля обязательно")
    private String name;

    @NotBlank(message = "Адрес обязателен")
    private String address;
}