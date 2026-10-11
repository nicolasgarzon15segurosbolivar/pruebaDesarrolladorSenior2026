package com.prueba.apipolizas.service;

import com.prueba.apipolizas.core.CoreClient;
import com.prueba.apipolizas.dto.CrearRiesgoRequest;
import com.prueba.apipolizas.exception.RecursoNoEncontradoException;
import com.prueba.apipolizas.exception.ReglaNegocioException;
import com.prueba.apipolizas.model.Poliza;
import com.prueba.apipolizas.model.Riesgo;
import com.prueba.apipolizas.model.enums.EstadoPoliza;
import com.prueba.apipolizas.model.enums.EstadoRiesgo;
import com.prueba.apipolizas.model.enums.TipoPoliza;
import com.prueba.apipolizas.repository.PolizaRepository;
import com.prueba.apipolizas.repository.RiesgoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Servicio de dominio para la gestion de polizas y riesgos.
 * Centraliza las reglas de negocio y la notificacion al CORE.
 */
@Service
public class PolizaService {

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;
    private final CoreClient coreClient;
    private final BigDecimal ipcPorcentaje;

    public PolizaService(PolizaRepository polizaRepository,
                         RiesgoRepository riesgoRepository,
                         CoreClient coreClient,
                         @Value("${app.renovacion.ipc-porcentaje}") BigDecimal ipcPorcentaje) {
        this.polizaRepository = polizaRepository;
        this.riesgoRepository = riesgoRepository;
        this.coreClient = coreClient;
        this.ipcPorcentaje = ipcPorcentaje;
    }

    /**
     * Lista polizas filtrando opcionalmente por tipo y/o estado, con paginacion.
     * Si un filtro llega nulo, no se aplica.
     */
    @Transactional(readOnly = true)
    public Page<Poliza> listar(TipoPoliza tipo, EstadoPoliza estado, Pageable pageable) {
        if (tipo != null && estado != null) {
            return polizaRepository.findByTipoAndEstado(tipo, estado, pageable);
        }
        if (tipo != null) {
            return polizaRepository.findByTipo(tipo, pageable);
        }
        if (estado != null) {
            return polizaRepository.findByEstado(estado, pageable);
        }
        return polizaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Poliza obtenerPoliza(Long id) {
        return polizaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la poliza con id " + id));
    }

    @Transactional(readOnly = true)
    public List<Riesgo> listarRiesgos(Long polizaId) {
        // Valida que la poliza exista antes de devolver los riesgos.
        obtenerPoliza(polizaId);
        return riesgoRepository.findByPolizaId(polizaId);
    }

    /**
     * Renueva una poliza:
     * - No se puede renovar una poliza CANCELADA.
     * - El canon se incrementa segun el IPC y la prima se recalcula.
     * - El estado pasa a RENOVADA.
     */
    @Transactional
    public Poliza renovar(Long id) {
        Poliza poliza = obtenerPoliza(id);

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException(
                    "No se puede renovar la poliza " + id + " porque esta CANCELADA");
        }

        // Incremento del canon segun IPC: canon * (1 + ipc/100)
        BigDecimal factor = BigDecimal.ONE.add(ipcPorcentaje.divide(BigDecimal.valueOf(100)));
        BigDecimal nuevoCanon = poliza.getCanonMensual()
                .multiply(factor)
                .setScale(2, RoundingMode.HALF_UP);

        poliza.setCanonMensual(nuevoCanon);
        poliza.recalcularPrima();
        poliza.setEstado(EstadoPoliza.RENOVADA);

        Poliza guardada = polizaRepository.save(poliza);
        coreClient.notificarEvento("RENOVACION", guardada.getId());
        return guardada;
    }

    /**
     * Cancela una poliza y, en cascada, todos sus riesgos activos.
     */
    @Transactional
    public Poliza cancelar(Long id) {
        Poliza poliza = obtenerPoliza(id);

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException(
                    "La poliza " + id + " ya se encuentra CANCELADA");
        }

        poliza.setEstado(EstadoPoliza.CANCELADA);
        // La cancelacion de la poliza cancela todos sus riesgos.
        poliza.getRiesgos().forEach(r -> r.setEstado(EstadoRiesgo.CANCELADO));

        Poliza guardada = polizaRepository.save(poliza);
        coreClient.notificarEvento("CANCELACION", guardada.getId());
        return guardada;
    }

    /**
     * Agrega un riesgo a una poliza.
     * Reglas:
     * - Solo polizas COLECTIVAS pueden agregar riesgos (la individual se maneja aparte).
     * - No se pueden agregar riesgos a una poliza cancelada.
     */
    @Transactional
    public Riesgo agregarRiesgo(Long polizaId, CrearRiesgoRequest request) {
        Poliza poliza = obtenerPoliza(polizaId);

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException(
                    "No se pueden agregar riesgos a la poliza " + polizaId + " porque esta CANCELADA");
        }

        if (poliza.getTipo() != TipoPoliza.COLECTIVA) {
            throw new ReglaNegocioException(
                    "Solo las polizas COLECTIVAS pueden agregar riesgos. La poliza "
                            + polizaId + " es " + poliza.getTipo());
        }

        Riesgo riesgo = new Riesgo();
        riesgo.setDireccionInmueble(request.direccionInmueble());
        riesgo.setArrendatario(request.arrendatario());
        riesgo.setEstado(EstadoRiesgo.ACTIVO);
        riesgo.setPoliza(poliza);
        poliza.getRiesgos().add(riesgo);

        // Guardamos directamente el riesgo para obtener el id generado en la respuesta.
        Riesgo guardado = riesgoRepository.save(riesgo);
        coreClient.notificarEvento("ACTUALIZACION", polizaId);
        return guardado;
    }

    /**
     * Cancela un riesgo individual por su id.
     */
    @Transactional
    public Riesgo cancelarRiesgo(Long riesgoId) {
        Riesgo riesgo = riesgoRepository.findById(riesgoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el riesgo con id " + riesgoId));

        if (riesgo.getEstado() == EstadoRiesgo.CANCELADO) {
            throw new ReglaNegocioException(
                    "El riesgo " + riesgoId + " ya se encuentra CANCELADO");
        }

        riesgo.setEstado(EstadoRiesgo.CANCELADO);
        Riesgo guardado = riesgoRepository.save(riesgo);
        coreClient.notificarEvento("ACTUALIZACION", riesgo.getPoliza().getId());
        return guardado;
    }
}
