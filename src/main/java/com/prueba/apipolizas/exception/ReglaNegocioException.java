package com.prueba.apipolizas.exception;

/**
 * Se lanza cuando se viola una regla de negocio (ej: renovar una poliza cancelada,
 * agregar un segundo riesgo a una poliza individual). Mapea a HTTP 409 / 400.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
