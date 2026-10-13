package org.campuslab.bff.service;

import org.campuslab.bff.client.NotifyClient;
import org.campuslab.bff.dto.DlqStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Servicio de orquestación para el estado de las DLQ (proxy delgado sobre ms-notify).
 */
@Service
public class NotifyService {

    private static final Logger logger = LoggerFactory.getLogger(NotifyService.class);

    private final NotifyClient notifyClient;

    public NotifyService(NotifyClient notifyClient) {
        this.notifyClient = notifyClient;
    }

    /**
     * Si ms-notify o RabbitMQ no responden se devuelve "UNAVAILABLE" en vez de
     * propagar el error, para que el frontend no muestre un "OK" falso.
     * (Se usa try/catch y no @CircuitBreaker: este proyecto no tiene AspectJ en
     * el classpath, así que esa anotación no se aplica.)
     */
    public DlqStatusDTO getDlqStatus() {
        try {
            DlqStatusDTO status = notifyClient.getDlqStatus().getBody();
            if (status == null) {
                return unavailable();
            }
            if (status.totalInDlq() > 0) {
                logger.warn("Hay {} mensaje(s) en las DLQ de RabbitMQ", status.totalInDlq());
            }
            return status;
        } catch (Exception ex) {
            logger.error("Error al obtener el estado de las DLQ de ms-notify", ex);
            return unavailable();
        }
    }

    private DlqStatusDTO unavailable() {
        return new DlqStatusDTO("UNAVAILABLE", 0, List.of(), Instant.now());
    }
}
