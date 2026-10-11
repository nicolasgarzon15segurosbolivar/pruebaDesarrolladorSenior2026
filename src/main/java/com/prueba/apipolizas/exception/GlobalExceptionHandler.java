package com.prueba.apipolizas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Manejo centralizado de errores para toda la API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404 - recurso no encontrado. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.de(HttpStatus.NOT_FOUND.value(), "No encontrado", ex.getMessage()));
    }

    /** 409 - violacion de regla de negocio. */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaNegocio(ReglaNegocioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.de(HttpStatus.CONFLICT.value(), "Conflicto de negocio", ex.getMessage()));
    }

    /** 400 - fallo de validacion de campos del request. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.de(HttpStatus.BAD_REQUEST.value(), "Validacion fallida", detalle));
    }

    /** 400 - parametro con tipo invalido (ej: tipo o estado fuera del enum). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        String mensaje = "Valor invalido para el parametro '" + ex.getName() + "': " + ex.getValue();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.de(HttpStatus.BAD_REQUEST.value(), "Parametro invalido", mensaje));
    }

    /** 500 - cualquier otro error no controlado. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.de(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Error interno", ex.getMessage()));
    }
}
