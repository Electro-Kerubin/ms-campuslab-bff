package org.campuslab.bff.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO para representar Reportes y KPIs.
 *
 * Datos agregados desde ms-report.
 */
public class ReportDTO {

    private String id;
    private String titulo;
    private String descripcion;
    private String tipo;
    private String fechaGeneracion;
    private String periodo;
    private Map<String, Object> datos;

    // Constructores
    public ReportDTO() {
        this.datos = new HashMap<>();
    }

    public ReportDTO(String id, String titulo, String tipo) {
        this.id = id;
        this.titulo = titulo;
        this.tipo = tipo;
        this.datos = new HashMap<>();
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(String fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Map<String, Object> getDatos() {
        return datos;
    }

    public void setDatos(Map<String, Object> datos) {
        this.datos = datos;
    }

    public void addDato(String clave, Object valor) {
        this.datos.put(clave, valor);
    }

    @Override
    public String toString() {
        return "ReportDTO{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", tipo='" + tipo + '\'' +
                ", periodo='" + periodo + '\'' +
                '}';
    }
}
