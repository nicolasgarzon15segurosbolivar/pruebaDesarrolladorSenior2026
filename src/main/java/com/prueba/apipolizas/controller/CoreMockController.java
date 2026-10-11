package com.prueba.apipolizas.controller;

import com.prueba.apipolizas.core.CoreEventoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint que simula la recepcion de eventos en el CORE transaccional legado.
 * Registra en el log la operacion recibida.
 *
 * Ejemplo de body:
 * {
 *   "evento": "ACTUALIZACION",
 *   "polizaId": 555
 * }
 */
@RestController
@RequestMapping("/core-mock")
public class CoreMockController {

    private static final Logger log = LoggerFactory.getLogger(CoreMockController.class);

    @PostMapping("/evento")
    public Map<String, Object> recibirEvento(@RequestBody CoreEventoRequest request) {
        log.info("[CORE-MOCK] Operacion recibida en el CORE -> evento='{}', polizaId={}",
                request.evento(), request.polizaId());
        return Map.of(
                "status", "RECIBIDO",
                "evento", request.evento(),
                "polizaId", request.polizaId()
        );
    }
}
