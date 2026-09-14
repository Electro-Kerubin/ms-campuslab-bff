package org.campuslab.bff.controller;

import org.campuslab.bff.dto.BookingDTO;
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
 * Expone API para el frontend Angular, orquestando llamadas a:
 * - ms-bookings: Gestión de reservas
 * - ms-catalog: Información de laboratorios
 *
 * Base Path: /api/bookings
 *
 * Roles disponibles:
 * - ADMIN: Acceso total
 * - TECNICO: Aprobación de reservas
 * - ESTUDIANTE: Crear y gestionar propias reservas
 * - AUDITOR: Solo lectura
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingApiController {

    // Constantes de roles para usar en @PreAuthorize
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_TECNICO = "TECNICO";
    private static final String ROLE_ESTUDIANTE = "ESTUDIANTE";
    private static final String ROLE_AUDITOR = "AUDITOR";

    @Autowired
    private BookingService bookingService;

    /**
     * GET /api/bookings
     * Obtener todas las reservas con filtros opcionales.
     *
     * Parámetros de query:
     * - labId: Filtrar por ID de laboratorio
     * - estado: Filtrar por estado (SOLICITADA, APROBADA, RECHAZADA, CANCELADA)
     * - estudianteId: Filtrar por ID de estudiante
     *
     * Acceso: TODOS (autenticados)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingDTO>> getAllBookings(
            @RequestParam(required = false) String labId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String estudianteId) {

        List<BookingDTO> bookings = bookingService.getAllBookings(labId, estado, estudianteId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * GET /api/bookings/{id}
     * Obtener una reserva específica por ID.
     *
     * Path Parameters:
     * - id: ID de la reserva
     *
     * Acceso: TODOS (autenticados)
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable String id) {
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
     *   "labId": "LAB-001",
     *   "fechaInicio": "2024-09-15T09:00:00",
     *   "fechaFin": "2024-09-15T11:00:00",
     *   "proposito": "Práctica de Programación",
     *   "capacidadEsperada": 25
     * }
     *
     * Acceso: ESTUDIANTE, ADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMIN')")
    public ResponseEntity<BookingDTO> createBooking(@RequestBody Map<String, Object> bookingData) {
        BookingDTO booking = bookingService.createBooking(bookingData);

        if (booking == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    /**
     * PUT /api/bookings/{id}
     * Actualizar una reserva existente.
     *
     * Path Parameters:
     * - id: ID de la reserva
     *
     * Body: Datos a actualizar (parcial o completo)
     *
     * Acceso: ESTUDIANTE (propia), ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMIN')")
    public ResponseEntity<BookingDTO> updateBooking(
            @PathVariable String id,
            @RequestBody Map<String, Object> bookingData) {

        BookingDTO booking = bookingService.updateBooking(id, bookingData);

        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(booking);
    }

    /**
     * DELETE /api/bookings/{id}
     * Cancelar una reserva.
     *
     * Path Parameters:
     * - id: ID de la reserva
     *
     * Acceso: ESTUDIANTE (propia), ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMIN')")
    public ResponseEntity<Void> cancelBooking(@PathVariable String id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/bookings/availability/{labId}
     * Verificar disponibilidad de un laboratorio en un rango de fechas.
     *
     * Path Parameters:
     * - labId: ID del laboratorio
     *
     * Query Parameters:
     * - fechaInicio: Fecha y hora inicio (ISO 8601)
     * - fechaFin: Fecha y hora fin (ISO 8601)
     *
     * Response:
     * {
     *   "disponible": true,
     *   "labId": "LAB-001",
     *   "conflictos": [],
     *   "capacidadDisponible": 25
     * }
     *
     * Acceso: TODOS (autenticados)
     */
    @GetMapping("/availability/{labId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable String labId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {

        Map<String, Object> availability = bookingService.checkAvailability(labId, fechaInicio, fechaFin);
        return ResponseEntity.ok(availability);
    }

    /**
     * POST /api/bookings/{id}/approve
     * Aprobar una reserva (solo TECNICO/ADMIN).
     *
     * Path Parameters:
     * - id: ID de la reserva
     *
     * Acceso: TECNICO, ADMIN
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<BookingDTO> approveBooking(@PathVariable String id) {
        BookingDTO booking = bookingService.approveBooking(id);

        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(booking);
    }

    /**
     * POST /api/bookings/{id}/reject
     * Rechazar una reserva con motivo (solo TECNICO/ADMIN).
     *
     * Path Parameters:
     * - id: ID de la reserva
     *
     * Body:
     * {
     *   "motivo": "Laboratorio no disponible en esa fecha"
     * }
     *
     * Acceso: TECNICO, ADMIN
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<BookingDTO> rejectBooking(
            @PathVariable String id,
            @RequestBody Map<String, String> razonRechazo) {

        // Este método debería estar en el servicio también
        // Por ahora retornamos un ejemplo
        Map<String, String> motivoRequest = new HashMap<>();
        motivoRequest.put("motivo", razonRechazo.getOrDefault("motivo", "No especificado"));

        BookingDTO booking = bookingService.getBookingById(id);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(booking);
    }

    /**
     * GET /api/bookings/estadisticas
     * Obtener estadísticas de reservas (solo ADMIN/TECNICO).
     *
     * Response:
     * {
     *   "totalReservas": 150,
     *   "aprobadas": 120,
     *   "pendientes": 20,
     *   "rechazadas": 10,
     *   "canceladas": 5,
     *   "tasaAprobacion": 0.85
     * }
     *
     * Acceso: ADMIN, TECNICO
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReservas", 150);
        stats.put("aprobadas", 120);
        stats.put("pendientes", 20);
        stats.put("rechazadas", 10);
        stats.put("canceladas", 5);
        stats.put("tasaAprobacion", 0.85);

        return ResponseEntity.ok(stats);
    }
}
