package org.campuslab.bff.dto;

/**
 * Cuerpo de POST/PUT /api/catalog/labs, mismo shape que LabRequestDTO de
 * ms-campuslab-catalog.
 */
public class LabRequestDTO {

    private String name;
    private String location;
    private Integer capacity;

    public LabRequestDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
