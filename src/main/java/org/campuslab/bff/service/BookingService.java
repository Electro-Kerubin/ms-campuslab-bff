package org.campuslab.bff.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.campuslab.bff.client.BookingsClient;
import org.campuslab.bff.client.CatalogClient;
import org.campuslab.bff.dto.BookingDTO;
import org.campuslab.bff.dto.CreateBookingDTO;
import org.campuslab.bff.dto.LabDTO;
import org.campuslab.bff.dto.StatusUpdateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de orquestación para operaciones de reservas.
 *
 * Agrega datos de múltiples microservicios:
 * - ms-bookings: Reservas (fuente de verdad)
 * - ms-catalog: Nombre del recurso reservado (best-effort, puede no estar disponible)
 */
@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingsClient bookingsClient;

    @Autowired
    private CatalogClient catalogClient;

    /**
     * Listar reservas con filtros opcionales.
     * status/from/to se pasan tal cual a ms-campuslab-bookings (from/to en
     * formato ISO-8601 LocalDateTime, ej: 2026-09-15T00:00:00).
     */
    @CircuitBreaker(name = "bookingService", fallbackMethod = "getAllBookingsFallback")
    public List<BookingDTO> getAllBookings(String status, String from, String to) {
        logger.info("Obteniendo reservas: status={}, from={}, to={}", status, from, to);

        ResponseEntity<List<BookingDTO>> bookingsResponse = bookingsClient.getAllBookings(status, from, to);
        List<BookingDTO> bookings = bookingsResponse.getBody();

        if (bookings == null || bookings.isEmpty()) {
            return List.of();
        }

        List<BookingDTO> enriched = bookings.stream()
                .peek(this::enrichWithResourceInfo)
                .collect(Collectors.toList());

        logger.info("Reservas obtenidas: {}", enriched.size());
        return enriched;
    }

    @CircuitBreaker(name = "bookingService", fallbackMethod = "getBookingByIdFallback")
    public BookingDTO getBookingById(Long id) {
        logger.info("Obteniendo reserva: id={}", id);

        ResponseEntity<BookingDTO> response = bookingsClient.getBookingById(id);
        BookingDTO booking = response.getBody();

        if (booking != null) {
            enrichWithResourceInfo(booking);
        }

        return booking;
    }

    @CircuitBreaker(name = "bookingService", fallbackMethod = "createBookingFallback")
    public BookingDTO createBooking(CreateBookingDTO request) {
        logger.info("Creando reserva: resourceId={}, startTime={}, endTime={}",
                request.getResourceId(), request.getStartTime(), request.getEndTime());

        ResponseEntity<BookingDTO> response = bookingsClient.createBooking(request);
        BookingDTO booking = response.getBody();

        if (booking != null) {
            enrichWithResourceInfo(booking);
        }

        return booking;
    }

    /**
     * Único mecanismo real de transición de estado. Aprobar/rechazar/cancelar
     * son casos de uso de este mismo endpoint (ver métodos de abajo).
     */
    @CircuitBreaker(name = "bookingService", fallbackMethod = "updateStatusFallback")
    public BookingDTO updateStatus(Long id, String status) {
        logger.info("Actualizando estado de reserva: id={}, status={}", id, status);

        ResponseEntity<BookingDTO> response = bookingsClient.updateStatus(id, new StatusUpdateDTO(status));
        BookingDTO booking = response.getBody();

        if (booking != null) {
            enrichWithResourceInfo(booking);
        }

        return booking;
    }

    public BookingDTO approveBooking(Long id) {
        return updateStatus(id, "APROBADA");
    }

    /**
     * ms-campuslab-bookings no tiene un estado RECHAZADA propio (ver
     * BookingStatus): la máquina de estados solo contempla
     * SOLICITADA → APROBADA → EN_PREPARACION → EN_USO → DEVUELTA, con
     * CANCELADA como salida desde cualquier estado intermedio. "Rechazar"
     * una solicitud se modela como cancelarla; el motivo solo queda en logs
     * (el backend no tiene dónde persistirlo todavía).
     */
    public BookingDTO rejectBooking(Long id, String motivo) {
        logger.info("Rechazando reserva id={} (se registra como CANCELADA). Motivo: {}", id, motivo);
        return updateStatus(id, "CANCELADA");
    }

    public BookingDTO cancelBooking(Long id) {
        return updateStatus(id, "CANCELADA");
    }

    /**
     * Agrega el nombre del recurso desde ms-catalog. No es un error de
     * negocio si ms-catalog no está disponible: simplemente no se muestra
     * el nombre y se sigue mostrando el resourceId.
     */
    private void enrichWithResourceInfo(BookingDTO booking) {
        if (booking.getResourceId() == null) {
            return;
        }
        try {
            ResponseEntity<LabDTO> labResponse = catalogClient.getLabById(booking.getResourceId().toString());
            if (labResponse.getBody() != null) {
                booking.setResourceNombre(labResponse.getBody().getNombre());
            }
        } catch (Exception e) {
            logger.debug("No se pudo obtener información del recurso {}: {}", booking.getResourceId(), e.getMessage());
        }
    }

    // Métodos fallback para Circuit Breaker
    public List<BookingDTO> getAllBookingsFallback(String status, String from, String to, Exception ex) {
        logger.error("Error al obtener reservas, retornando lista vacía", ex);
        return List.of();
    }

    public BookingDTO getBookingByIdFallback(Long id, Exception ex) {
        logger.error("Error al obtener reserva {}", id, ex);
        return null;
    }

    public BookingDTO createBookingFallback(CreateBookingDTO request, Exception ex) {
        logger.error("Error al crear reserva", ex);
        return null;
    }

    public BookingDTO updateStatusFallback(Long id, String status, Exception ex) {
        logger.error("Error al actualizar estado de reserva {}", id, ex);
        return null;
    }
}
