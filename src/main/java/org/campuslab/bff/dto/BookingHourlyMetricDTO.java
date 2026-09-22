package org.campuslab.bff.dto;

/**
 * Refleja BookingHourlyMetricResponse de ms-campuslab-report.
 *
 * bucketHour/generado a partir de eventos "SOLICITADA" leídos de Kafka
 * (topic bookings.events) — si Kafka no está corriendo o no hubo reservas
 * en el rango pedido, esta lista viene vacía.
 */
public class BookingHourlyMetricDTO {

    private Long labId;
    private String bucketHour;
    private Integer bookingsCount;
    private String avgCycleMinutes;

    public BookingHourlyMetricDTO() {
    }

    public Long getLabId() {
        return labId;
    }

    public void setLabId(Long labId) {
        this.labId = labId;
    }

    public String getBucketHour() {
        return bucketHour;
    }

    public void setBucketHour(String bucketHour) {
        this.bucketHour = bucketHour;
    }

    public Integer getBookingsCount() {
        return bookingsCount;
    }

    public void setBookingsCount(Integer bookingsCount) {
        this.bookingsCount = bookingsCount;
    }

    public String getAvgCycleMinutes() {
        return avgCycleMinutes;
    }

    public void setAvgCycleMinutes(String avgCycleMinutes) {
        this.avgCycleMinutes = avgCycleMinutes;
    }
}
