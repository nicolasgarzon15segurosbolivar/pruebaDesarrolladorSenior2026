package com.prueba.apipolizas.repository;

import com.prueba.apipolizas.model.Poliza;
import com.prueba.apipolizas.model.enums.EstadoPoliza;
import com.prueba.apipolizas.model.enums.TipoPoliza;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de Polizas. Spring Data genera la implementacion a partir de los nombres de metodo.
 */
@Repository
public interface PolizaRepository extends JpaRepository<Poliza, Long> {

    Page<Poliza> findByTipo(TipoPoliza tipo, Pageable pageable);

    Page<Poliza> findByEstado(EstadoPoliza estado, Pageable pageable);

    Page<Poliza> findByTipoAndEstado(TipoPoliza tipo, EstadoPoliza estado, Pageable pageable);
}
