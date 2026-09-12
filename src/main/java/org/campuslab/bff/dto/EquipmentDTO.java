package org.campuslab.bff.dto;

/**
 * DTO para representar un Equipo.
 *
 * Datos agregados desde ms-catalog.
 */
public class EquipmentDTO {

    private String id;
    private String nombre;
    private String descripcion;
    private String labId;
    private String labNombre;
    private String estado;
    private int cantidad;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String ultimoMantenimiento;
    private String proximoMantenimiento;

    // Constructores
    public EquipmentDTO() {
    }

    public EquipmentDTO(String id, String nombre, String labId, String estado, int cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.labId = labId;
        this.estado = estado;
        this.cantidad = cantidad;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getLabId() {
        return labId;
    }

    public void setLabId(String labId) {
        this.labId = labId;
    }

    public String getLabNombre() {
        return labNombre;
    }

    public void setLabNombre(String labNombre) {
        this.labNombre = labNombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getUltimoMantenimiento() {
        return ultimoMantenimiento;
    }

    public void setUltimoMantenimiento(String ultimoMantenimiento) {
        this.ultimoMantenimiento = ultimoMantenimiento;
    }

    public String getProximoMantenimiento() {
        return proximoMantenimiento;
    }

    public void setProximoMantenimiento(String proximoMantenimiento) {
        this.proximoMantenimiento = proximoMantenimiento;
    }

    @Override
    public String toString() {
        return "EquipmentDTO{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", labNombre='" + labNombre + '\'' +
                ", estado='" + estado + '\'' +
                ", cantidad=" + cantidad +
                '}';
    }
}
