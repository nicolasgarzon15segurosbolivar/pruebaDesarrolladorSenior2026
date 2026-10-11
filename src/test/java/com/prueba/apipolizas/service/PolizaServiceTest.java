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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolizaServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private RiesgoRepository riesgoRepository;

    @Mock
    private CoreClient coreClient;

    private PolizaService service;

    @BeforeEach
    void setUp() {
        // IPC del 5.5% para las pruebas de renovacion
        service = new PolizaService(polizaRepository, riesgoRepository, coreClient, new BigDecimal("5.5"));
    }

    private Poliza nuevaPoliza(Long id, TipoPoliza tipo, EstadoPoliza estado) {
        Poliza poliza = new Poliza();
        poliza.setId(id);
        poliza.setTipo(tipo);
        poliza.setEstado(estado);
        poliza.setTomador("Tomador de prueba");
        poliza.setVigenciaInicio(LocalDate.of(2025, 1, 1));
        poliza.setVigenciaFin(LocalDate.of(2025, 12, 31));
        poliza.setMesesVigencia(12);
        poliza.setCanonMensual(new BigDecimal("2000000.00"));
        poliza.setPrima(new BigDecimal("24000000.00"));
        return poliza;
    }

    // --- listar ---

    private final Pageable pageable = PageRequest.of(0, 20);

    @Test
    void listarSinFiltrosDevuelveTodas() {
        Page<Poliza> pagina = new PageImpl<>(List.of(nuevaPoliza(1L, TipoPoliza.INDIVIDUAL, EstadoPoliza.VIGENTE)));
        when(polizaRepository.findAll(pageable)).thenReturn(pagina);

        Page<Poliza> resultado = service.listar(null, null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(polizaRepository).findAll(pageable);
    }

    @Test
    void listarConTipoYEstadoUsaConsultaCombinada() {
        Page<Poliza> pagina = new PageImpl<>(List.of(nuevaPoliza(2L, TipoPoliza.COLECTIVA, EstadoPoliza.VIGENTE)));
        when(polizaRepository.findByTipoAndEstado(TipoPoliza.COLECTIVA, EstadoPoliza.VIGENTE, pageable))
                .thenReturn(pagina);

        Page<Poliza> resultado = service.listar(TipoPoliza.COLECTIVA, EstadoPoliza.VIGENTE, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(polizaRepository).findByTipoAndEstado(TipoPoliza.COLECTIVA, EstadoPoliza.VIGENTE, pageable);
    }

    @Test
    void listarSoloConTipoUsaConsultaPorTipo() {
        when(polizaRepository.findByTipo(TipoPoliza.INDIVIDUAL, pageable)).thenReturn(Page.empty());

        service.listar(TipoPoliza.INDIVIDUAL, null, pageable);

        verify(polizaRepository).findByTipo(TipoPoliza.INDIVIDUAL, pageable);
    }

    @Test
    void listarSoloConEstadoUsaConsultaPorEstado() {
        when(polizaRepository.findByEstado(EstadoPoliza.CANCELADA, pageable)).thenReturn(Page.empty());

        service.listar(null, EstadoPoliza.CANCELADA, pageable);

        verify(polizaRepository).findByEstado(EstadoPoliza.CANCELADA, pageable);
    }

    // --- obtenerPoliza ---

    @Test
    void obtenerPolizaInexistenteLanzaNoEncontrada() {
        when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPoliza(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    // --- renovar ---

    @Test
    void renovarIncrementaCanonYPrimaYCambiaEstado() {
        Poliza poliza = nuevaPoliza(1L, TipoPoliza.COLECTIVA, EstadoPoliza.VIGENTE);
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));
        when(polizaRepository.save(any(Poliza.class))).thenAnswer(inv -> inv.getArgument(0));

        Poliza resultado = service.renovar(1L);

        // 2.000.000 * 1.055 = 2.110.000
        assertThat(resultado.getCanonMensual()).isEqualByComparingTo("2110000.00");
        // prima = canon * 12 meses = 25.320.000
        assertThat(resultado.getPrima()).isEqualByComparingTo("25320000.00");
        assertThat(resultado.getEstado()).isEqualTo(EstadoPoliza.RENOVADA);
        verify(coreClient).notificarEvento(eq("RENOVACION"), eq(1L));
    }

    @Test
    void noSePuedeRenovarPolizaCancelada() {
        Poliza poliza = nuevaPoliza(1L, TipoPoliza.COLECTIVA, EstadoPoliza.CANCELADA);
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));

        assertThatThrownBy(() -> service.renovar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("CANCELADA");

        verify(polizaRepository, never()).save(any());
        verify(coreClient, never()).notificarEvento(any(), anyLong());
    }

    // --- cancelar ---

    @Test
    void cancelarPolizaCancelaTodosSusRiesgos() {
        Poliza poliza = nuevaPoliza(2L, TipoPoliza.COLECTIVA, EstadoPoliza.VIGENTE);
        Riesgo r1 = new Riesgo();
        r1.setEstado(EstadoRiesgo.ACTIVO);
        Riesgo r2 = new Riesgo();
        r2.setEstado(EstadoRiesgo.ACTIVO);
        poliza.setRiesgos(new ArrayList<>(List.of(r1, r2)));

        when(polizaRepository.findById(2L)).thenReturn(Optional.of(poliza));
        when(polizaRepository.save(any(Poliza.class))).thenAnswer(inv -> inv.getArgument(0));

        Poliza resultado = service.cancelar(2L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoPoliza.CANCELADA);
        assertThat(resultado.getRiesgos())
                .allMatch(r -> r.getEstado() == EstadoRiesgo.CANCELADO);
        verify(coreClient).notificarEvento(eq("CANCELACION"), eq(2L));
    }

    @Test
    void noSePuedeCancelarUnaPolizaYaCancelada() {
        Poliza poliza = nuevaPoliza(2L, TipoPoliza.COLECTIVA, EstadoPoliza.CANCELADA);
        when(polizaRepository.findById(2L)).thenReturn(Optional.of(poliza));

        assertThatThrownBy(() -> service.cancelar(2L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya se encuentra CANCELADA");
    }

    // --- agregarRiesgo ---

    @Test
    void agregarRiesgoAColectivaFunciona() {
        Poliza poliza = nuevaPoliza(2L, TipoPoliza.COLECTIVA, EstadoPoliza.VIGENTE);
        poliza.setRiesgos(new ArrayList<>());
        when(polizaRepository.findById(2L)).thenReturn(Optional.of(poliza));
        when(riesgoRepository.save(any(Riesgo.class))).thenAnswer(inv -> inv.getArgument(0));

        CrearRiesgoRequest request = new CrearRiesgoRequest("Calle 50 #10-10", "Sofia Perez");
        Riesgo resultado = service.agregarRiesgo(2L, request);

        assertThat(resultado.getDireccionInmueble()).isEqualTo("Calle 50 #10-10");
        assertThat(resultado.getArrendatario()).isEqualTo("Sofia Perez");
        assertThat(resultado.getEstado()).isEqualTo(EstadoRiesgo.ACTIVO);
        verify(coreClient).notificarEvento(eq("ACTUALIZACION"), eq(2L));
    }

    @Test
    void noSePuedeAgregarRiesgoAUnaPolizaIndividual() {
        Poliza poliza = nuevaPoliza(1L, TipoPoliza.INDIVIDUAL, EstadoPoliza.VIGENTE);
        poliza.setRiesgos(new ArrayList<>());
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));

        CrearRiesgoRequest request = new CrearRiesgoRequest("Carrera 1 #1-1", "Pedro Gomez");

        assertThatThrownBy(() -> service.agregarRiesgo(1L, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("COLECTIVAS");

        verify(riesgoRepository, never()).save(any());
    }

    @Test
    void noSePuedeAgregarRiesgoAUnaPolizaCancelada() {
        Poliza poliza = nuevaPoliza(2L, TipoPoliza.COLECTIVA, EstadoPoliza.CANCELADA);
        when(polizaRepository.findById(2L)).thenReturn(Optional.of(poliza));

        CrearRiesgoRequest request = new CrearRiesgoRequest("Calle X", "Juan");

        assertThatThrownBy(() -> service.agregarRiesgo(2L, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("CANCELADA");
    }

    // --- cancelarRiesgo ---

    @Test
    void cancelarRiesgoFunciona() {
        Poliza poliza = nuevaPoliza(2L, TipoPoliza.COLECTIVA, EstadoPoliza.VIGENTE);
        Riesgo riesgo = new Riesgo();
        riesgo.setId(5L);
        riesgo.setEstado(EstadoRiesgo.ACTIVO);
        riesgo.setPoliza(poliza);

        when(riesgoRepository.findById(5L)).thenReturn(Optional.of(riesgo));
        when(riesgoRepository.save(any(Riesgo.class))).thenAnswer(inv -> inv.getArgument(0));

        Riesgo resultado = service.cancelarRiesgo(5L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoRiesgo.CANCELADO);
        verify(coreClient).notificarEvento(eq("ACTUALIZACION"), eq(2L));
    }

    @Test
    void noSePuedeCancelarUnRiesgoYaCancelado() {
        Riesgo riesgo = new Riesgo();
        riesgo.setId(5L);
        riesgo.setEstado(EstadoRiesgo.CANCELADO);

        when(riesgoRepository.findById(5L)).thenReturn(Optional.of(riesgo));

        assertThatThrownBy(() -> service.cancelarRiesgo(5L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya se encuentra CANCELADO");
    }

    @Test
    void cancelarRiesgoInexistenteLanzaNoEncontrado() {
        when(riesgoRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelarRiesgo(123L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("123");
    }
}
