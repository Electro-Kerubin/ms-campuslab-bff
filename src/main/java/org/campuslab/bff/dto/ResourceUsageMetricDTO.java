package org.campuslab.bff.dto;

/**
 * Refleja ResourceUsageMetricResponse de ms-campuslab-report.
 * Igual que BookingHourlyMetricDTO, depende de eventos Kafka reales.
 */
public class ResourceUsageMetricDTO {

    private Long resourceId;
    private String periodStart;
    private String periodEnd;
    private Integer usageCount;
    private String totalDurationMinutes;

    public ResourceUsageMetricDTO() {
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(String periodStart) {
        this.periodStart = periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(String periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public String getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(String totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }
}
