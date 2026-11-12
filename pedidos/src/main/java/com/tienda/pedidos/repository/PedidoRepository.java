package com.tienda.pedidos.repository;

import com.tienda.pedidos.entity.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * DAO de pedidos. Reemplaza el SQL concatenado con JdbcTemplate del código de
 * partida: el CRUD lo hereda de JpaRepository y las consultas derivadas se
 * generan como sentencias parametrizadas (sin riesgo de inyección SQL).
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteEmailOrderByIdAsc(String clienteEmail);
}
