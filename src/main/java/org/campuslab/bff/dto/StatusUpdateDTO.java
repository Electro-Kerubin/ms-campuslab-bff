package org.campuslab.bff.dto;

/**
 * Cuerpo del PUT /api/bookings/{id}/status.
 *
 * Valores válidos (enum BookingStatus de ms-campuslab-bookings):
 * SOLICITADA, APROBADA, EN_PREPARACION, EN_USO, DEVUELTA, CANCELADA.
 */
public class StatusUpdateDTO {

    private String status;

    public StatusUpdateDTO() {
    }

    public StatusUpdateDTO(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
