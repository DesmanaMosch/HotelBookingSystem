package com.hotel.booking.repository;

import com.hotel.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    Optional<Booking> findByRequestId(String requestId);

    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.roomId = :roomId
        AND b.status IN ('PENDING', 'CONFIRMED')
        AND b.startDate < :endDate
        AND b.endDate > :startDate
    """)
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}