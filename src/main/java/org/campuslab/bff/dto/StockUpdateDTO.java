package org.campuslab.bff.dto;

/** Cuerpo de PUT /api/catalog/resources/{id}/stock. */
public class StockUpdateDTO {

    private Integer delta;
    private Long referenceBookingId;
    private String note;

    public StockUpdateDTO() {
    }

    public StockUpdateDTO(Integer delta, Long referenceBookingId, String note) {
        this.delta = delta;
        this.referenceBookingId = referenceBookingId;
        this.note = note;
    }

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public Long getReferenceBookingId() {
        return referenceBookingId;
    }

    public void setReferenceBookingId(Long referenceBookingId) {
        this.referenceBookingId = referenceBookingId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
