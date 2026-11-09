package com.hotel.booking.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class HotelServiceClient {

    private final RestTemplate restTemplate;
    private final String hotelServiceUrl;
    private final int maxRetries;

    public HotelServiceClient(
            RestTemplate restTemplate,
            @Value("${hotel-service.url:http://localhost:8082}") String hotelServiceUrl,
            @Value("${hotel-service.max-retries:3}") int maxRetries) {
        this.restTemplate = restTemplate;
        this.hotelServiceUrl = hotelServiceUrl;
        this.maxRetries = maxRetries;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoomDto {
        private Long id;
        private Long hotelId;
        private String number;
        private boolean available;
        private int timesBooked;
    }

    @Data
    public static class AvailabilityRequest {
        private LocalDate startDate;
        private LocalDate endDate;
        private String requestId;

        public AvailabilityRequest(LocalDate startDate,
                                   LocalDate endDate,
                                   String requestId) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.requestId = requestId;
        }
    }

    public List<RoomDto> getRecommendedRooms() {
        try {
            ResponseEntity<RoomDto[]> response = restTemplate.getForEntity(
                    hotelServiceUrl + "/api/rooms/recommend",
                    RoomDto[].class
            );
            return response.getBody() != null
                    ? List.of(response.getBody())
                    : List.of();
        } catch (Exception e) {
            log.error("Ошибка получения рекомендованных номеров: {}", e.getMessage());
            return List.of();
        }
    }

    public boolean confirmAvailability(Long roomId,
                                       LocalDate startDate,
                                       LocalDate endDate,
                                       String requestId) {
        AvailabilityRequest request =
                new AvailabilityRequest(startDate, endDate, requestId);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Подтверждение номера id={}, попытка {}/{}",
                        roomId, attempt, maxRetries);

                restTemplate.postForEntity(
                        hotelServiceUrl + "/api/rooms/" + roomId + "/confirm-availability",
                        request,
                        Void.class
                );
                return true;

            } catch (Exception e) {
                log.warn("Попытка {}/{} неудачна для номера id={}: {}",
                        attempt, maxRetries, roomId, e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000L * (long) Math.pow(2, attempt - 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }

        log.error("Все {} попытки исчерпаны для номера id={}", maxRetries, roomId);
        return false;
    }

    public void releaseRoom(Long roomId, String requestId) {
        try {
            restTemplate.postForEntity(
                    hotelServiceUrl + "/api/rooms/" + roomId
                            + "/release?requestId=" + requestId,
                    null,
                    Void.class
            );
            log.info("Номер id={} освобождён (компенсация)", roomId);
        } catch (Exception e) {
            log.error("Ошибка при освобождении номера id={}: {}", roomId, e.getMessage());
        }
    }
}