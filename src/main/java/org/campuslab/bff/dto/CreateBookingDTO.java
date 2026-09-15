package org.campuslab.bff.dto;

/**
 * Cuerpo del POST /api/bookings recibido desde el frontend y reenviado tal
 * cual a ms-campuslab-bookings (mismo shape que su record BookingRequest).
 *
 * studentEmail puede venir vacío: ms-campuslab-bookings lo completa desde el
 * JWT (claim "email"/"preferred_username") cuando el que reserva es el mismo
 * estudiante autenticado.
 */
public class CreateBookingDTO {

    private Long resourceId;
    private String studentEmail;
    private String purpose;
    private String startTime;
    private String endTime;

    public CreateBookingDTO() {
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
