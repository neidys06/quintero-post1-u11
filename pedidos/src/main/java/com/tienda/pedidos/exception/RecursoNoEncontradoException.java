package com.tienda.pedidos.exception;

/** El recurso pedido no existe; el GlobalExceptionHandler lo traduce a 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " con id " + id + " no encontrado.");
    }
}
