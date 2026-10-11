package com.prueba.apipolizas.controller;

import com.prueba.apipolizas.dto.RiesgoResponse;
import com.prueba.apipolizas.service.PolizaService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de gestion directa de riesgos.
 */
@RestController
@RequestMapping("/riesgos")
public class RiesgoController {

    private final PolizaService polizaService;

    public RiesgoController(PolizaService polizaService) {
        this.polizaService = polizaService;
    }

    /** Cancela un riesgo por su identificador. */
    @PostMapping("/{id}/cancelar")
    public RiesgoResponse cancelar(@PathVariable Long id) {
        return RiesgoResponse.desde(polizaService.cancelarRiesgo(id));
    }
}
