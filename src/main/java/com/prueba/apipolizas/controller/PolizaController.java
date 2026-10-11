package com.prueba.apipolizas.controller;

import com.prueba.apipolizas.dto.CrearRiesgoRequest;
import com.prueba.apipolizas.dto.PolizaResponse;
import com.prueba.apipolizas.dto.RiesgoResponse;
import com.prueba.apipolizas.model.enums.EstadoPoliza;
import com.prueba.apipolizas.model.enums.TipoPoliza;
import com.prueba.apipolizas.service.PolizaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de gestion de polizas y riesgos.
 */
@RestController
@RequestMapping("/polizas")
public class PolizaController {

    private final PolizaService polizaService;

    public PolizaController(PolizaService polizaService) {
        this.polizaService = polizaService;
    }

    /**
     * Lista las polizas, con filtros opcionales por tipo y estado y paginacion.
     * Parametros de paginacion: page, size y sort (ej: ?page=0&size=20&sort=id,desc).
     */
    @GetMapping
    public Page<PolizaResponse> listar(
            @RequestParam(required = false) TipoPoliza tipo,
            @RequestParam(required = false) EstadoPoliza estado,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return polizaService.listar(tipo, estado, pageable)
                .map(PolizaResponse::desde);
    }

    /** GET /polizas/{id} -> consultar una poliza. */
    @GetMapping("/{id}")
    public PolizaResponse obtener(@PathVariable Long id) {
        return PolizaResponse.desde(polizaService.obtenerPoliza(id));
    }

    /** Devuelve los riesgos asociados a una poliza. */
    @GetMapping("/{id}/riesgos")
    public List<RiesgoResponse> listarRiesgos(@PathVariable Long id) {
        return polizaService.listarRiesgos(id).stream()
                .map(RiesgoResponse::desde)
                .toList();
    }

    /** Renueva una poliza: incrementa canon y prima por IPC y la deja en estado RENOVADA. */
    @PostMapping("/{id}/renovar")
    public PolizaResponse renovar(@PathVariable Long id) {
        return PolizaResponse.desde(polizaService.renovar(id));
    }

    /** Cancela la poliza y, en cascada, todos sus riesgos. */
    @PostMapping("/{id}/cancelar")
    public PolizaResponse cancelar(@PathVariable Long id) {
        return PolizaResponse.desde(polizaService.cancelar(id));
    }

    /** Agrega un riesgo a la poliza (solo permitido en polizas colectivas). */
    @PostMapping("/{id}/riesgos")
    public ResponseEntity<RiesgoResponse> agregarRiesgo(
            @PathVariable Long id,
            @Valid @RequestBody CrearRiesgoRequest request) {
        RiesgoResponse response = RiesgoResponse.desde(polizaService.agregarRiesgo(id, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
