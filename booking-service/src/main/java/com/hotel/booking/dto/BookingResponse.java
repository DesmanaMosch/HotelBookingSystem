package com.hotel.booking.dto;

import com.hotel.booking.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long userId;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BookingStatus status;
    private LocalDateTime createdAt;
}