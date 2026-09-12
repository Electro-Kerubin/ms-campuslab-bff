package org.campuslab.bff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de endpoints públicos - Accesibles sin autenticación.
 *
 * Ejemplos de endpoints públicos (sin restricción de rol).
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    /**
     * Obtener información general del sistema (Sin autenticación).
     */
    @GetMapping("/informacion")
    public ResponseEntity<Map<String, String>> obtenerInformacion() {
        Map<String, String> info = new HashMap<>();
        info.put("nombre_aplicacion", "CampusLab BFF");
        info.put("version", "1.0.0");
        info.put("descripcion", "Backend For Frontend - Puerta de enlace para microservicios");
        info.put("estado", "OPERATIVO");

        return ResponseEntity.ok(info);
    }

    /**
     * Endpoint de salud (Health Check).
     * Usado por load balancers y monitoring.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return ResponseEntity.ok(health);
    }

    /**
     * Registrar nuevo usuario (Sin autenticación inicialmente).
     * Después del registro, el usuario tendrá rol ESTUDIANTE.
     */
    @PostMapping("/registro")
    public ResponseEntity<Map<String, String>> registrarUsuario(
            @RequestBody Map<String, String> datos) {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario registrado exitosamente");
        response.put("email", datos.get("email"));
        response.put("rol_asignado", "ESTUDIANTE");
        response.put("usuario_id", "USR-NEW-001");

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener términos y condiciones (Sin autenticación).
     */
    @GetMapping("/terminos-condiciones")
    public ResponseEntity<Map<String, String>> obtenerTerminos() {
        Map<String, String> terminos = new HashMap<>();
        terminos.put("version", "1.0");
        terminos.put("fecha_vigencia", "2024-01-01");
        terminos.put("contenido", "Términos y condiciones de uso de CampusLab...");

        return ResponseEntity.ok(terminos);
    }

    /**
     * Obtener política de privacidad (Sin autenticación).
     */
    @GetMapping("/politica-privacidad")
    public ResponseEntity<Map<String, String>> obtenerPoliticaPrivacidad() {
        Map<String, String> politica = new HashMap<>();
        politica.put("version", "1.0");
        politica.put("fecha_vigencia", "2024-01-01");
        politica.put("contenido", "Política de privacidad de CampusLab...");

        return ResponseEntity.ok(politica);
    }
}
