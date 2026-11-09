package com.hotel.hotelservice.service;

import com.hotel.hotelservice.dto.AvailabilityRequest;
import com.hotel.hotelservice.dto.RoomRequest;
import com.hotel.hotelservice.dto.RoomResponse;
import com.hotel.hotelservice.entity.Hotel;
import com.hotel.hotelservice.entity.Room;
import com.hotel.hotelservice.repository.HotelRepository;
import com.hotel.hotelservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    private final ConcurrentHashMap<String, Boolean> processedRequests =
            new ConcurrentHashMap<>();

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Отель не найден: " + request.getHotelId()));

        Room room = new Room();
        room.setHotel(hotel);
        room.setNumber(request.getNumber());
        room.setAvailable(true);
        room.setTimesBooked(0);

        Room saved = roomRepository.save(room);
        log.debug("Создан номер id={} в отеле id={}", saved.getId(), hotel.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getAllAvailableRooms() {
        return roomRepository.findByAvailableTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getRecommendedRooms() {
        return roomRepository.findByAvailableTrueOrderByTimesBookedAscIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void confirmAvailability(Long roomId, AvailabilityRequest request) {
        String requestId = request.getRequestId();

        if (processedRequests.containsKey(requestId)) {
            log.info("Повторный запрос requestId={} — пропускаем", requestId);
            return;
        }

        Room room = roomRepository.findByIdWithLock(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Номер не найден: " + roomId));

        if (!room.isAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Номер недоступен (выведен из эксплуатации)");
        }

        room.setTimesBooked(room.getTimesBooked() + 1);
        roomRepository.save(room);

        processedRequests.put(requestId, true);

        log.info("Номер id={} подтверждён, requestId={}, timesBooked={}",
                roomId, requestId, room.getTimesBooked());
    }

    @Transactional
    public void releaseRoom(Long roomId, String requestId) {
        if (!processedRequests.containsKey(requestId)) {
            log.warn("release вызван для неизвестного requestId={} — пропускаем", requestId);
            return;
        }

        Room room = roomRepository.findByIdWithLock(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Номер не найден: " + roomId));

        if (room.getTimesBooked() > 0) {
            room.setTimesBooked(room.getTimesBooked() - 1);
            roomRepository.save(room);
        }

        processedRequests.remove(requestId);

        log.info("Номер id={} освобождён (компенсация), requestId={}", roomId, requestId);
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getHotel().getId(),
                room.getNumber(),
                room.isAvailable(),
                room.getTimesBooked()
        );
    }
}