package org.campuslab.bff.client;

import org.campuslab.bff.dto.BookingDTO;
import org.campuslab.bff.dto.CreateBookingDTO;
import org.campuslab.bff.dto.StatusUpdateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cliente Feign para comunicación con ms-bookings.
 *
 * Los endpoints reflejan exactamente el BookingController real de
 * ms-campuslab-bookings:
 *   POST /api/bookings
 *   GET  /api/bookings/{id}
 *   GET  /api/bookings?status=&from=&to=
 *   PUT  /api/bookings/{id}/status
 */
@FeignClient(
        name = "ms-bookings",
        url = "${microservice.bookings.url:http://localhost:8081}",
        configuration = org.campuslab.bff.config.FeignConfig.class
)
public interface BookingsClient {

    @PostMapping("/api/bookings")
    ResponseEntity<BookingDTO> createBooking(@RequestBody CreateBookingDTO booking);

    @GetMapping("/api/bookings/{id}")
    ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id);

    /**
     * Lista con filtros opcionales. from/to en formato ISO-8601 LocalDateTime
     * (ej: 2026-09-15T00:00:00), igual que espera ms-campuslab-bookings.
     */
    @GetMapping("/api/bookings")
    ResponseEntity<List<BookingDTO>> getAllBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    );

    /**
     * Único mecanismo de transición de estado que ofrece el microservicio
     * (solo TECNICO/ADMIN). Aprobar, rechazar, cancelar, etc. se resuelven
     * todos llamando aquí con el estado destino correspondiente.
     */
    @PutMapping("/api/bookings/{id}/status")
    ResponseEntity<BookingDTO> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateDTO status);
}
