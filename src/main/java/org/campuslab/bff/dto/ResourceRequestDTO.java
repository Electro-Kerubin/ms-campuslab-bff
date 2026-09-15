package org.campuslab.bff.dto;

/**
 * Cuerpo de POST /api/catalog/resources, mismo shape que
 * ResourceRequestDTO (con su record anidado EquipmentData) de
 * ms-campuslab-catalog.
 */
public class ResourceRequestDTO {

    private Long labId;
    private Long categoryId;
    private String name;
    private String resourceType;
    private String status;
    private Integer quantityTotal;
    private Integer reorderThreshold;
    private EquipmentData equipment;
    private String unitOfMeasure;

    public ResourceRequestDTO() {
    }

    public Long getLabId() {
        return labId;
    }

    public void setLabId(Long labId) {
        this.labId = labId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getQuantityTotal() {
        return quantityTotal;
    }

    public void setQuantityTotal(Integer quantityTotal) {
        this.quantityTotal = quantityTotal;
    }

    public Integer getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public EquipmentData getEquipment() {
        return equipment;
    }

    public void setEquipment(EquipmentData equipment) {
        this.equipment = equipment;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public static class EquipmentData {
        private String brand;
        private String model;
        private String serialNumber;

        public EquipmentData() {
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }
    }
}
