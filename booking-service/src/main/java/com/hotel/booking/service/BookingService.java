package com.hotel.booking.service;

import com.hotel.booking.client.HotelServiceClient;
import com.hotel.booking.dto.BookingRequest;
import com.hotel.booking.dto.BookingResponse;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final HotelServiceClient hotelServiceClient;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Пользователь не найден"));

        Long roomId = resolveRoomId(request);

        if (bookingRepository.existsOverlappingBooking(
                roomId, request.getStartDate(), request.getEndDate())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Номер уже забронирован на эти даты");
        }

        String requestId = UUID.randomUUID().toString();

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoomId(roomId);
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setRequestId(requestId);

        booking = bookingRepository.save(booking);
        log.info("Создано бронирование id={} статус=PENDING, requestId={}",
                booking.getId(), requestId);

        boolean confirmed = hotelServiceClient.confirmAvailability(
                roomId,
                request.getStartDate(),
                request.getEndDate(),
                requestId
        );

        if (confirmed) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            log.info("Бронирование id={} → CONFIRMED", booking.getId());
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            log.warn("Бронирование id={} → CANCELLED (компенсация)", booking.getId());

            hotelServiceClient.releaseRoom(roomId, requestId);

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Не удалось подтвердить доступность номера");
        }

        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Пользователь не найден"));

        return bookingRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, String username) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Бронирование не найдено"));

        if (!booking.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Нет доступа к этому бронированию");
        }

        return toResponse(booking);
    }

    @Transactional
    public void cancelBooking(Long id, String username) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Бронирование не найдено"));

        if (!booking.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Нет доступа к этому бронированию");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Бронирование уже отменено");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);


        hotelServiceClient.releaseRoom(booking.getRoomId(), booking.getRequestId());
        log.info("Бронирование id={} отменено пользователем {}", id, username);
    }

    private Long resolveRoomId(BookingRequest request) {
        if (!request.isAutoSelect()) {
            if (request.getRoomId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Укажите roomId или включите autoSelect");
            }
            return request.getRoomId();
        }

        List<HotelServiceClient.RoomDto> rooms =
                hotelServiceClient.getRecommendedRooms();

        if (rooms.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Нет доступных номеров");
        }

        Long roomId = rooms.get(0).getId();
        log.info("Автоподбор: выбран номер id={}", roomId);
        return roomId;
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getRoomId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}