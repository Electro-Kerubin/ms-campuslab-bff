package org.campuslab.bff.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.campuslab.bff.client.BookingsClient;
import org.campuslab.bff.client.CatalogClient;
import org.campuslab.bff.dto.BookingDTO;
import org.campuslab.bff.dto.LabDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de orquestación para operaciones de reservas.
 *
 * Agrega datos de múltiples microservicios:
 * - ms-bookings: Reservas
 * - ms-catalog: Información de laboratorios
 */
@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingsClient bookingsClient;

    @Autowired
    private CatalogClient catalogClient;

    /**
     * Obtener todas las reservas con información agregada del catálogo.
     */
    @CircuitBreaker(name = "bookingService", fallbackMethod = "getAllBookingsFallback")
    public List<BookingDTO> getAllBookings(String labId, String estado, String estudianteId) {
        logger.info("Obteniendo reservas: labId={}, estado={}, estudianteId={}", labId, estado, estudianteId);

        // Obtener reservas desde ms-bookings
        ResponseEntity<List<BookingDTO>> bookingsResponse = bookingsClient.getAllBookings(labId, estado, estudianteId);
        List<BookingDTO> bookings = bookingsResponse.getBody();

        if (bookings == null || bookings.isEmpty()) {
            return List.of();
        }

        // Enriquecer con información del catálogo
        List<BookingDTO> enrichedBookings = bookings.stream()
                .peek(booking -> enrichBookingWithLabInfo(booking))
                .collect(Collectors.toList());

        logger.info("Reservas obtenidas: {}", enrichedBookings.size());
        return enrichedBookings;
    }

    /**
     * Obtener una reserva específica con información agregada.
     */
    @CircuitBreaker(name = "bookingService", fallbackMethod = "getBookingByIdFallback")
    public BookingDTO getBookingById(String id) {
        logger.info("Obteniendo reserva: id={}", id);

        ResponseEntity<BookingDTO> response = bookingsClient.getBookingById(id);
        BookingDTO booking = response.getBody();

        if (booking != null) {
            enrichBookingWithLabInfo(booking);
        }

        return booking;
    }

    /**
     * Crear una nueva reserva.
     */
    @CircuitBreaker(name = "bookingService", fallbackMethod = "createBookingFallback")
    public BookingDTO createBooking(Map<String, Object> bookingData) {
        logger.info("Creando reserva: {}", bookingData);

        ResponseEntity<BookingDTO> response = bookingsClient.createBooking(bookingData);
        BookingDTO booking = response.getBody();

        if (booking != null) {
            enrichBookingWithLabInfo(booking);
        }

        return booking;
    }

    /**
     * Actualizar una reserva existente.
     */
    @CircuitBreaker(name = "bookingService", fallbackMethod = "updateBookingFallback")
    public BookingDTO updateBooking(String id, Map<String, Object> bookingData) {
        logger.info("Actualizando reserva: id={}", id);

        ResponseEntity<BookingDTO> response = bookingsClient.updateBooking(id, bookingData);
        BookingDTO booking = response.getBody();

        if (booking != null) {
            enrichBookingWithLabInfo(booking);
        }

        return booking;
    }

    /**
     * Cancelar una reserva.
     */
    @CircuitBreaker(name = "bookingService", fallbackMethod = "cancelBookingFallback")
    public void cancelBooking(String id) {
        logger.info("Cancelando reserva: id={}", id);
        bookingsClient.cancelBooking(id);
    }

    /**
     * Verificar disponibilidad de laboratorio.
     */
    @CircuitBreaker(name = "bookingService", fallbackMethod = "checkAvailabilityFallback")
    public Map<String, Object> checkAvailability(String labId, String fechaInicio, String fechaFin) {
        logger.info("Verificando disponibilidad: labId={}, fechaInicio={}, fechaFin={}",
                   labId, fechaInicio, fechaFin);

        ResponseEntity<Map<String, Object>> response = bookingsClient.checkAvailability(labId, fechaInicio, fechaFin);
        return response.getBody();
    }

    /**
     * Aprobar una reserva.
     */
    @CircuitBreaker(name = "bookingService", fallbackMethod = "approveBookingFallback")
    public BookingDTO approveBooking(String id) {
        logger.info("Aprobando reserva: id={}", id);

        ResponseEntity<BookingDTO> response = bookingsClient.approveBooking(id);
        return response.getBody();
    }

    /**
     * Enriquecer información de reserva con datos del catálogo.
     */
    private void enrichBookingWithLabInfo(BookingDTO booking) {
        if (booking.getLabId() != null) {
            try {
                ResponseEntity<LabDTO> labResponse = catalogClient.getLabById(booking.getLabId());
                if (labResponse.getBody() != null) {
                    booking.setLabNombre(labResponse.getBody().getNombre());
                }
            } catch (Exception e) {
                logger.warn("No se pudo obtener información del laboratorio: {}", booking.getLabId(), e);
            }
        }
    }

    // Métodos fallback para Circuit Breaker
    public List<BookingDTO> getAllBookingsFallback(String labId, String estado, String estudianteId, Exception ex) {
        logger.error("Error al obtener reservas, retornando lista vacía", ex);
        return List.of();
    }

    public BookingDTO getBookingByIdFallback(String id, Exception ex) {
        logger.error("Error al obtener reserva {}", id, ex);
        return null;
    }

    public BookingDTO createBookingFallback(Map<String, Object> bookingData, Exception ex) {
        logger.error("Error al crear reserva", ex);
        return null;
    }

    public BookingDTO updateBookingFallback(String id, Map<String, Object> bookingData, Exception ex) {
        logger.error("Error al actualizar reserva {}", id, ex);
        return null;
    }

    public void cancelBookingFallback(String id, Exception ex) {
        logger.error("Error al cancelar reserva {}", id, ex);
    }

    public Map<String, Object> checkAvailabilityFallback(String labId, String fechaInicio, String fechaFin, Exception ex) {
        logger.error("Error al verificar disponibilidad", ex);
        Map<String, Object> result = new HashMap<>();
        result.put("disponible", false);
        result.put("motivo", "Servicio no disponible");
        return result;
    }

    public BookingDTO approveBookingFallback(String id, Exception ex) {
        logger.error("Error al aprobar reserva {}", id, ex);
        return null;
    }
}
