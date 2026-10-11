package com.prueba.apipolizas.repository;

import com.prueba.apipolizas.model.Riesgo;
import com.prueba.apipolizas.model.enums.EstadoRiesgo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de Riesgos.
 */
@Repository
public interface RiesgoRepository extends JpaRepository<Riesgo, Long> {

    List<Riesgo> findByPolizaId(Long polizaId);

    long countByPolizaIdAndEstado(Long polizaId, EstadoRiesgo estado);
}
