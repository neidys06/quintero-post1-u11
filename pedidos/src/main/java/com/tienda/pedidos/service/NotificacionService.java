package com.tienda.pedidos.service;

/** Abstracción del envío de notificaciones: el servicio no conoce la implementación. */
public interface NotificacionService {

    void enviarConfirmacion(Long pedidoId, String clienteEmail, double total);
}
