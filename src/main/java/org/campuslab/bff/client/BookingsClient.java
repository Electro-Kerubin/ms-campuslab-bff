package org.campuslab.bff.client;

import org.campuslab.bff.dto.BookingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Cliente Feign para comunicación con ms-bookings.
 *
 * Maneja todas las operaciones relacionadas con reservas de laboratorios.
 */
@FeignClient(
        name = "ms-bookings",
        url = "${microservice.bookings.url:http://localhost:8081}",
        configuration = org.campuslab.bff.config.FeignConfig.class
)
public interface BookingsClient {

    /**
     * Obtener todas las reservas con filtros opcionales.
     */
    @GetMapping("/api/bookings")
    ResponseEntity<List<BookingDTO>> getAllBookings(
            @RequestParam(required = false) String labId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String estudianteId
    );

    /**
     * Obtener una reserva específica por ID.
     */
    @GetMapping("/api/bookings/{id}")
    ResponseEntity<BookingDTO> getBookingById(@PathVariable String id);

    /**
     * Crear una nueva reserva.
     */
    @PostMapping("/api/bookings")
    ResponseEntity<BookingDTO> createBooking(@RequestBody Map<String, Object> bookingData);

    /**
     * Actualizar una reserva existente.
     */
    @PutMapping("/api/bookings/{id}")
    ResponseEntity<BookingDTO> updateBooking(
            @PathVariable String id,
            @RequestBody Map<String, Object> bookingData
    );

    /**
     * Cancelar una reserva.
     */
    @DeleteMapping("/api/bookings/{id}")
    ResponseEntity<Void> cancelBooking(@PathVariable String id);

    /**
     * Obtener disponibilidad de laboratorio en un rango de fechas.
     */
    @GetMapping("/api/bookings/availability/{labId}")
    ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable String labId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin
    );

    /**
     * Aprobar una reserva (solo TECNICO/ADMIN).
     */
    @PostMapping("/api/bookings/{id}/approve")
    ResponseEntity<BookingDTO> approveBooking(@PathVariable String id);

    /**
     * Rechazar una reserva (solo TECNICO/ADMIN).
     */
    @PostMapping("/api/bookings/{id}/reject")
    ResponseEntity<BookingDTO> rejectBooking(
            @PathVariable String id,
            @RequestBody Map<String, String> razonRechazo
    );
}
