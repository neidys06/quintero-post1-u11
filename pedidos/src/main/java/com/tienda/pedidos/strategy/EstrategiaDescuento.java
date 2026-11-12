package com.tienda.pedidos.strategy;

/**
 * Patrón Strategy: cada tipo de cliente tiene su forma de calcular el descuento.
 * Las implementaciones se registran como beans cuyo NOMBRE es el tipo de cliente
 * (REGULAR, VIP, MAYORISTA); Spring las inyecta como Map<String, EstrategiaDescuento>.
 */
public interface EstrategiaDescuento {

    double calcularDescuento(double subtotal);
}
