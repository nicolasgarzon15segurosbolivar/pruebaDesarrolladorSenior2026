package com.prueba.apipolizas.model;

import com.prueba.apipolizas.model.enums.EstadoPoliza;
import com.prueba.apipolizas.model.enums.TipoPoliza;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Poliza.
 *
 * Reglas de dominio relevantes:
 * - El valor de la prima = canon mensual * numero de meses de vigencia.
 * - Una poliza INDIVIDUAL solo puede tener 1 riesgo; una COLECTIVA puede tener muchos.
 */
@Entity
@Table(name = "polizas")
public class Poliza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPoliza tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPoliza estado;

    /** Nombre del tomador de la poliza (arrendatario en individual, inmobiliaria/administracion en colectiva). */
    @Column(nullable = false)
    private String tomador;

    /** Inicio del periodo de vigencia. */
    @Column(name = "vigencia_inicio", nullable = false)
    private LocalDate vigenciaInicio;

    /** Fin del periodo de vigencia. */
    @Column(name = "vigencia_fin", nullable = false)
    private LocalDate vigenciaFin;

    /** Numero de meses de la vigencia (usado para calcular la prima). */
    @Column(name = "meses_vigencia", nullable = false)
    private Integer mesesVigencia;

    /** Valor del canon mensual de arrendamiento. */
    @Column(name = "canon_mensual", nullable = false, precision = 15, scale = 2)
    private BigDecimal canonMensual;

    /** Valor de la prima = canon mensual * meses de vigencia. */
    @Column(name = "prima", nullable = false, precision = 15, scale = 2)
    private BigDecimal prima;

    @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Riesgo> riesgos = new ArrayList<>();

    public Poliza() {
    }

    /** Recalcula la prima a partir del canon mensual y los meses de vigencia. */
    public void recalcularPrima() {
        if (canonMensual != null && mesesVigencia != null) {
            this.prima = canonMensual.multiply(BigDecimal.valueOf(mesesVigencia));
        }
    }

    public void addRiesgo(Riesgo riesgo) {
        riesgos.add(riesgo);
        riesgo.setPoliza(this);
    }

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoPoliza getTipo() {
        return tipo;
    }

    public void setTipo(TipoPoliza tipo) {
        this.tipo = tipo;
    }

    public EstadoPoliza getEstado() {
        return estado;
    }

    public void setEstado(EstadoPoliza estado) {
        this.estado = estado;
    }

    public String getTomador() {
        return tomador;
    }

    public void setTomador(String tomador) {
        this.tomador = tomador;
    }

    public LocalDate getVigenciaInicio() {
        return vigenciaInicio;
    }

    public void setVigenciaInicio(LocalDate vigenciaInicio) {
        this.vigenciaInicio = vigenciaInicio;
    }

    public LocalDate getVigenciaFin() {
        return vigenciaFin;
    }

    public void setVigenciaFin(LocalDate vigenciaFin) {
        this.vigenciaFin = vigenciaFin;
    }

    public Integer getMesesVigencia() {
        return mesesVigencia;
    }

    public void setMesesVigencia(Integer mesesVigencia) {
        this.mesesVigencia = mesesVigencia;
    }

    public BigDecimal getCanonMensual() {
        return canonMensual;
    }

    public void setCanonMensual(BigDecimal canonMensual) {
        this.canonMensual = canonMensual;
    }

    public BigDecimal getPrima() {
        return prima;
    }

    public void setPrima(BigDecimal prima) {
        this.prima = prima;
    }

    public List<Riesgo> getRiesgos() {
        return riesgos;
    }

    public void setRiesgos(List<Riesgo> riesgos) {
        this.riesgos = riesgos;
    }
}
