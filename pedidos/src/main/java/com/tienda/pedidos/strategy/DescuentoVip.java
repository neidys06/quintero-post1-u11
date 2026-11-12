package com.tienda.pedidos.strategy;

import org.springframework.stereotype.Component;

/** Cliente VIP: 15 % de descuento sobre el subtotal. */
@Component("VIP")
public class DescuentoVip implements EstrategiaDescuento {

    private static final double PORCENTAJE = 0.15;

    @Override
    public double calcularDescuento(double subtotal) {
        return subtotal * PORCENTAJE;
    }
}
