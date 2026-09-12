package org.campuslab.bff.dto;

/**
 * DTO para representar una Reserva de Laboratorio.
 *
 * Datos agregados desde ms-bookings.
 */
public class BookingDTO {

    private String id;
    private String labId;
    private String labNombre;
    private String estudianteId;
    private String estudianteNombre;
    private String fechaInicio;
    private String fechaFin;
    private String proposito;
    private String estado;
    private int capacidadEsperada;
    private String fechaCreacion;
    private String tecnicoAprobador;
    private String observaciones;

    // Constructores
    public BookingDTO() {
    }

    public BookingDTO(String id, String labId, String estudianteId,
                     String fechaInicio, String fechaFin, String estado) {
        this.id = id;
        this.labId = labId;
        this.estudianteId = estudianteId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(String estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getEstudianteNombre() {
        return estudianteNombre;
    }

    public void setEstudianteNombre(String estudianteNombre) {
        this.estudianteNombre = estudianteNombre;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getProposito() {
        return proposito;
    }

    public void setProposito(String proposito) {
        this.proposito = proposito;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getCapacidadEsperada() {
        return capacidadEsperada;
    }

    public void setCapacidadEsperada(int capacidadEsperada) {
        this.capacidadEsperada = capacidadEsperada;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getTecnicoAprobador() {
        return tecnicoAprobador;
    }

    public void setTecnicoAprobador(String tecnicoAprobador) {
        this.tecnicoAprobador = tecnicoAprobador;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "BookingDTO{" +
                "id='" + id + '\'' +
                ", labNombre='" + labNombre + '\'' +
                ", estudianteNombre='" + estudianteNombre + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
