package com.hotel.booking.controller;

import com.hotel.booking.dto.BookingRequest;
import com.hotel.booking.dto.BookingResponse;
import com.hotel.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/booking")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @Valid @RequestBody BookingRequest request,
            Principal principal) {
        System.out.println(">>> CREATE BOOKING CONTROLLER REACHED");
        System.out.println(">>> USER: " + principal.getName());
        return bookingService.createBooking(request, principal.getName());
    }

    @GetMapping("/bookings")
    public List<BookingResponse> getUserBookings(Principal principal) {
        return bookingService.getUserBookings(principal.getName());
    }

    @GetMapping("/booking/{id}")
    public BookingResponse getBooking(
            @PathVariable Long id,
            Principal principal) {
        return bookingService.getBookingById(id, principal.getName());
    }

    @DeleteMapping("/booking/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelBooking(
            @PathVariable Long id,
            Principal principal) {
        bookingService.cancelBooking(id, principal.getName());
    }
}