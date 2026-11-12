package com.tienda.pedidos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Implementación por correo (simulada: el envío real está fuera del alcance). */
@Service
public class NotificacionEmailServiceImpl implements NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionEmailServiceImpl.class);

    @Override
    public void enviarConfirmacion(Long pedidoId, String clienteEmail, double total) {
        log.info("Correo de confirmación enviado: pedidoId={}, clienteEmail={}, total={}",
                pedidoId, clienteEmail, total);
    }
}
