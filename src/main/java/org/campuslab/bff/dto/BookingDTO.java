package org.campuslab.bff.dto;

/**
 * DTO para representar una Reserva de Laboratorio.
 *
 * Refleja exactamente el {@code BookingResponse} que expone
 * ms-campuslab-bookings (ver su record BookingResponse), más un campo
 * opcional {@code resourceNombre} agregado desde ms-catalog cuando está
 * disponible.
 */
public class BookingDTO {

    private Long id;
    private Long resourceId;
    private String resourceNombre;
    private String studentEmail;
    private String purpose;
    private String startTime;
    private String endTime;
    private String status;
    private String createdAt;
    private String updatedAt;

    public BookingDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceNombre() {
        return resourceNombre;
    }

    public void setResourceNombre(String resourceNombre) {
        this.resourceNombre = resourceNombre;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "BookingDTO{" +
                "id=" + id +
                ", resourceId=" + resourceId +
                ", studentEmail='" + studentEmail + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
