package com.tienda.pedidos.strategy;

import org.springframework.stereotype.Component;

/** Cliente MAYORISTA: 25 % de descuento sobre el subtotal. */
@Component("MAYORISTA")
public class DescuentoMayorista implements EstrategiaDescuento {

    private static final double PORCENTAJE = 0.25;

    @Override
    public double calcularDescuento(double subtotal) {
        return subtotal * PORCENTAJE;
    }
}
