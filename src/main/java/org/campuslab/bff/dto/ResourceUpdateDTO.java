package org.campuslab.bff.dto;

/** Cuerpo de PUT /api/catalog/resources/{id}. */
public class ResourceUpdateDTO {

    private String name;
    private String status;

    public ResourceUpdateDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
