package org.campuslab.bff.dto;

/**
 * DTO para representar un Laboratorio.
 *
 * Datos agregados desde ms-catalog.
 */
public class LabDTO {

    private String id;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private int capacidad;
    private String estado;
    private String responsable;
    private String horarioApertura;
    private String horarioCierre;
    private int equiposDisponibles;

    // Constructores
    public LabDTO() {
    }

    public LabDTO(String id, String nombre, String descripcion, String ubicacion,
                  int capacidad, String estado, String responsable) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.capacidad = capacidad;
        this.estado = estado;
        this.responsable = responsable;
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

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getHorarioApertura() {
        return horarioApertura;
    }

    public void setHorarioApertura(String horarioApertura) {
        this.horarioApertura = horarioApertura;
    }

    public String getHorarioCierre() {
        return horarioCierre;
    }

    public void setHorarioCierre(String horarioCierre) {
        this.horarioCierre = horarioCierre;
    }

    public int getEquiposDisponibles() {
        return equiposDisponibles;
    }

    public void setEquiposDisponibles(int equiposDisponibles) {
        this.equiposDisponibles = equiposDisponibles;
    }

    @Override
    public String toString() {
        return "LabDTO{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", capacidad=" + capacidad +
                ", estado='" + estado + '\'' +
                '}';
    }
}
