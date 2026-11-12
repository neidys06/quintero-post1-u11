package com.tienda.pedidos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.entity.Pedido;
import com.tienda.pedidos.exception.RecursoNoEncontradoException;
import com.tienda.pedidos.repository.PedidoRepository;
import com.tienda.pedidos.strategy.DescuentoMayorista;
import com.tienda.pedidos.strategy.DescuentoRegular;
import com.tienda.pedidos.strategy.DescuentoVip;
import com.tienda.pedidos.strategy.EstrategiaDescuento;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Gracias a DIP, el servicio se prueba sin Spring: las dependencias se pasan
 * por constructor (repositorio y notificador simulados, estrategias reales).
 */
class PedidoServiceImplTest {

    private PedidoRepository repo;
    private NotificacionService notificador;
    private PedidoServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(PedidoRepository.class);
        notificador = mock(NotificacionService.class);
        Map<String, EstrategiaDescuento> estrategias = Map.of(
                "REGULAR", new DescuentoRegular(),
                "VIP", new DescuentoVip(),
                "MAYORISTA", new DescuentoMayorista());
        service = new PedidoServiceImpl(repo, notificador, estrategias);
        when(repo.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
    }

    @Test
    void crear_clienteVip_aplica15PorCientoEIva() {
        PedidoResponseDTO r = service.crear(request("VIP", 100000.0));

        assertThat(r.getDescuento()).isEqualTo(15000.0);
        assertThat(r.getIva()).isEqualTo(16150.0);
        assertThat(r.getTotal()).isEqualTo(101150.0);
        verify(notificador).enviarConfirmacion(1L, "ana@correo.com", 101150.0);
    }

    @Test
    void crear_clienteMayorista_aplica25PorCiento() {
        assertThat(service.crear(request("MAYORISTA", 100000.0)).getDescuento()).isEqualTo(25000.0);
    }

    @Test
    void crear_clienteRegular_sinDescuento() {
        assertThat(service.crear(request("regular", 100000.0)).getDescuento()).isEqualTo(0.0);
    }

    @Test
    void crear_tipoDesconocido_usaEstrategiaRegular() {
        PedidoResponseDTO r = service.crear(request("ORO", 100000.0));

        assertThat(r.getDescuento()).isEqualTo(0.0);
        assertThat(r.getTipoCliente()).isEqualTo("REGULAR");
    }

    @Test
    void buscarPorId_noExiste_lanzaRecursoNoEncontrado() {
        when(repo.findById(999L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex =
                assertThrows(RecursoNoEncontradoException.class, () -> service.buscarPorId(999L));
        assertThat(ex.getMessage()).isEqualTo("Pedido con id 999 no encontrado.");
    }

    private PedidoRequestDTO request(String tipo, double subtotal) {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setClienteEmail("ana@correo.com");
        dto.setTipoCliente(tipo);
        dto.setSubtotal(subtotal);
        return dto;
    }
}
