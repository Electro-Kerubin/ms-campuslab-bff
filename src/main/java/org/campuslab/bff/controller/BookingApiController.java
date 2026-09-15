package org.campuslab.bff.controller;

import org.campuslab.bff.dto.BookingDTO;
import org.campuslab.bff.dto.CreateBookingDTO;
import org.campuslab.bff.dto.StatusUpdateDTO;
import org.campuslab.bff.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para operaciones de Reservas.
 *
 * Expone al frontend Angular el mismo contrato que ofrece
 * ms-campuslab-bookings (ver su BookingController), agregando el nombre del
 * recurso desde ms-catalog cuando está disponible.
 *
 * Base Path: /api/bookings
 *
 * Roles disponibles:
 * - ADMIN: Acceso total
 * - TECNICO: Aprueba/rechaza/cambia el estado de las reservas
 * - ESTUDIANTE: Crea y consulta reservas
 * - AUDITOR: Solo lectura
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingApiController {

    @Autowired
    private BookingService bookingService;

    /**
     * GET /api/bookings
     * Lista reservas con filtros opcionales (los mismos que soporta
     * ms-campuslab-bookings).
     *
     * Query params:
     * - status: SOLICITADA | APROBADA | EN_PREPARACION | EN_USO | DEVUELTA | CANCELADA
     * - from / to: fecha-hora ISO-8601, ej. 2026-09-15T00:00:00
     *
     * Acceso: TODOS (autenticados)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingDTO>> getAllBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        List<BookingDTO> bookings = bookingService.getAllBookings(status, from, to);
        return ResponseEntity.ok(bookings);
    }

    /**
     * GET /api/bookings/{id}
     * Acceso: TODOS (autenticados)
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        BookingDTO booking = bookingService.getBookingById(id);

        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(booking);
    }

    /**
     * POST /api/bookings
     * Crear una nueva reserva.
     *
     * Body:
     * {
     *   "resourceId": 1,
     *   "purpose": "Práctica de Programación",
     *   "startTime": "2026-09-15T09:00:00",
     *   "endTime": "2026-09-15T11:00:00"
     * }
     * "studentEmail" es opcional: si no viene, ms-campuslab-bookings lo toma del JWT.
     *
     * Acceso: ESTUDIANTE, TECNICO, ADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'TECNICO', 'ADMIN')")
    public ResponseEntity<BookingDTO> createBooking(@RequestBody CreateBookingDTO request) {
        BookingDTO booking = bookingService.createBooking(request);

        if (booking == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    /**
     * PUT /api/bookings/{id}/status
     * Cambia el estado de una reserva a cualquiera de los valores válidos
     * del enum BookingStatus. Es el mecanismo genérico; approve/reject más
     * abajo son atajos sobre este mismo endpoint.
     *
     * Acceso: TECNICO, ADMIN
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<BookingDTO> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateDTO body) {
        BookingDTO booking = bookingService.updateStatus(id, body.getStatus());

        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(booking);
    }

    /**
     * DELETE /api/bookings/{id}
     * Cancela una reserva (equivalente a PUT .../status con CANCELADA).
     *
     * Acceso: ESTUDIANTE, TECNICO, ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'TECNICO', 'ADMIN')")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long id) {
        BookingDTO booking = bookingService.cancelBooking(id);

        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(booking);
    }

    /**
     * POST /api/bookings/{id}/approve
     * Acceso: TECNICO, ADMIN
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<BookingDTO> approveBooking(@PathVariable Long id) {
        BookingDTO booking = bookingService.approveBooking(id);

        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(booking);
    }

    /**
     * POST /api/bookings/{id}/reject
     * ms-campuslab-bookings no tiene un estado "RECHAZADA" propio: esto se
     * resuelve como una cancelación (ver BookingService.rejectBooking). El
     * motivo se registra solo en logs por ahora.
     *
     * Body: { "motivo": "Laboratorio no disponible en esa fecha" }
     *
     * Acceso: TECNICO, ADMIN
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<BookingDTO> rejectBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        String motivo = body != null ? body.getOrDefault("motivo", "No especificado") : "No especificado";
        BookingDTO booking = bookingService.rejectBooking(id, motivo);

        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(booking);
    }

    /**
     * GET /api/bookings/estadisticas
     *
     * ⚠️ Placeholder: ms-campuslab-bookings todavía no expone un endpoint de
     * estadísticas agregadas, así que estos números son de ejemplo y no
     * reflejan datos reales. Reemplazar cuando exista el endpoint real.
     *
     * Acceso: ADMIN, TECNICO
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReservas", 0);
        stats.put("aprobadas", 0);
        stats.put("pendientes", 0);
        stats.put("canceladas", 0);
        stats.put("nota", "Placeholder: ms-campuslab-bookings aún no expone estadísticas reales");

        return ResponseEntity.ok(stats);
    }
}
