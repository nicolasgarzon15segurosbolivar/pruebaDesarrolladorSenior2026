package com.prueba.apipolizas.core;

/**
 * Payload del evento que se envia al CORE transaccional legado (a traves de la capa media en WebLogic).
 */
public record CoreEventoRequest(String evento, Long polizaId) {
}
