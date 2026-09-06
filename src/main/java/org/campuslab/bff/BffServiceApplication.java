package org.campuslab.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * BFF (Backend For Frontend) Service Application
 *
 * Punto de entrada para el servicio BFF que actúa como puerta de enlace entre el frontend
 * y los microservicios de dominio (bookings, catalog, audit, report).
 *
 * Responsabilidades:
 * - Validar JWT y aplicar seguridad
 * - Enrutar requests a los microservicios correspondientes
 * - Implementar circuito de fallos y reintentos
 * - Agregar datos de múltiples servicios si es necesario
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "org.campuslab.bff.client")
@ComponentScan(basePackages = "org.campuslab.bff")
public class BffServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffServiceApplication.class, args);
    }

}
