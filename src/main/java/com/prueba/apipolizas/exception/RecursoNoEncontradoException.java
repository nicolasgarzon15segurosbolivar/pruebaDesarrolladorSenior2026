package com.prueba.apipolizas.exception;

/**
 * Se lanza cuando no se encuentra una poliza o riesgo solicitado. Mapea a HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
