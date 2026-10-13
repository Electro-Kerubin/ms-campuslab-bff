package org.campuslab.bff.client;

import org.campuslab.bff.dto.DlqStatusDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Cliente Feign para comunicación con ms-notify.
 *
 * ms-notify es un consumidor de RabbitMQ; su único endpoint HTTP es de solo
 * lectura y devuelve cuántos mensajes hay en las colas de mensajes fallidos (DLQ).
 */
@FeignClient(
        name = "ms-notify",
        url = "${microservice.notify.url:http://localhost:8084}",
        configuration = org.campuslab.bff.config.FeignConfig.class
)
public interface NotifyClient {

    @GetMapping("/api/notify/dlq")
    ResponseEntity<DlqStatusDTO> getDlqStatus();
}
