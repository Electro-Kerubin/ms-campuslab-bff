package org.campuslab.bff.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de estudiante - Accesible por ESTUDIANTE y ADMIN.
 *
 * Ejemplos de endpoints para acceso a cursos, calificaciones, etc.
 */
@RestController
@RequestMapping("/api/estudiante")
public class EstudianteController {

    /**
     * Obtener mis cursos inscritos (ESTUDIANTE, ADMIN).
     */
    @GetMapping("/mis-cursos")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerMisCursos() {
        Map<String, Object> cursos = new HashMap<>();
        List<Map<String, String>> listaCursos = new ArrayList<>();

        Map<String, String> curso1 = new HashMap<>();
        curso1.put("id", "CURSO-001");
        curso1.put("nombre", "Introducción a Java");
        curso1.put("profesor", "Dr. García");
        curso1.put("estado", "ACTIVO");

        Map<String, String> curso2 = new HashMap<>();
        curso2.put("id", "CURSO-002");
        curso2.put("nombre", "Bases de Datos SQL");
        curso2.put("profesor", "Ing. López");
        curso2.put("estado", "ACTIVO");

        listaCursos.add(curso1);
        listaCursos.add(curso2);

        cursos.put("total_cursos", 2);
        cursos.put("cursos", listaCursos);

        return ResponseEntity.ok(cursos);
    }

    /**
     * Obtener mis calificaciones (ESTUDIANTE, ADMIN).
     */
    @GetMapping("/mis-calificaciones")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerMisCalificaciones() {
        Map<String, Object> calificaciones = new HashMap<>();
        List<Map<String, Object>> listaCalificaciones = new ArrayList<>();

        Map<String, Object> cal1 = new HashMap<>();
        cal1.put("curso", "Introducción a Java");
        cal1.put("nota", 7.5);
        cal1.put("fecha", "2024-09-12");

        Map<String, Object> cal2 = new HashMap<>();
        cal2.put("curso", "Bases de Datos SQL");
        cal2.put("nota", 8.0);
        cal2.put("fecha", "2024-09-10");

        listaCalificaciones.add(cal1);
        listaCalificaciones.add(cal2);

        calificaciones.put("promedio", 7.75);
        calificaciones.put("calificaciones", listaCalificaciones);

        return ResponseEntity.ok(calificaciones);
    }

    /**
     * Enviar tarea (ESTUDIANTE, ADMIN).
     */
    @PostMapping("/enviar-tarea")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMIN')")
    public ResponseEntity<Map<String, String>> enviarTarea(
            @RequestBody Map<String, String> tarea) {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Tarea enviada correctamente");
        response.put("id_tarea", "TAREA-12345");
        response.put("fecha_envio", "2024-09-12T10:30:00");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener mis materiales de curso (ESTUDIANTE, ADMIN).
     */
    @GetMapping("/mis-materiales")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerMisMateriales() {
        Map<String, Object> materiales = new HashMap<>();
        List<Map<String, String>> listaMateriales = new ArrayList<>();

        Map<String, String> mat1 = new HashMap<>();
        mat1.put("nombre", "Introducción a POO.pdf");
        mat1.put("curso", "Introducción a Java");
        mat1.put("fecha_subida", "2024-09-01");
        mat1.put("tamaño_kb", "2048");

        listaMateriales.add(mat1);

        materiales.put("total_materiales", 1);
        materiales.put("materiales", listaMateriales);

        return ResponseEntity.ok(materiales);
    }

    /**
     * Ver mi perfil de estudiante.
     */
    @GetMapping("/mi-perfil")
    public ResponseEntity<Map<String, Object>> obtenerMiPerfil(
            Authentication authentication) {
        Map<String, Object> perfil = new HashMap<>();
        perfil.put("usuario", authentication.getName());
        perfil.put("rol", authentication.getAuthorities());
        perfil.put("autenticado", authentication.isAuthenticated());

        return ResponseEntity.ok(perfil);
    }
}
