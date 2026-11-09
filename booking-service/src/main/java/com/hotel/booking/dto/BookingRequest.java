package com.hotel.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BookingRequest {

    private boolean autoSelect = false;

    private Long roomId;

    @NotNull(message = "Дата заезда обязательна")
    private LocalDate startDate;

    @NotNull(message = "Дата выезда обязательна")
    private LocalDate endDate;
}