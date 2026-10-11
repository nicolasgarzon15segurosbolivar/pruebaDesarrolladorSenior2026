package com.prueba.apipolizas.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adapter de integracion con el CORE de seguros.
 *
 * Invoca el servicio agnostico de edicion expuesto por la capa media en WebLogic,
 * que mantiene actualizado el CORE. Toda accion que modifica el estado de polizas
 * o riesgos pasa por aqui.
 */
@Component
public class CoreClient {

    private static final Logger log = LoggerFactory.getLogger(CoreClient.class);

    /**
     * Notifica al CORE que ocurrio una actualizacion sobre una poliza.
     *
     * @param evento   tipo de evento (ej: ACTUALIZACION, RENOVACION, CANCELACION)
     * @param polizaId identificador de la poliza afectada
     */
    public void notificarEvento(String evento, Long polizaId) {
        CoreEventoRequest request = new CoreEventoRequest(evento, polizaId);
        // TODO: reemplazar por la llamada HTTP/SOAP real al servicio en WebLogic (con reintentos y circuit breaker).
        log.info("[CORE] Enviando evento al CORE de seguros -> evento='{}', polizaId={}",
                request.evento(), request.polizaId());
    }
}
