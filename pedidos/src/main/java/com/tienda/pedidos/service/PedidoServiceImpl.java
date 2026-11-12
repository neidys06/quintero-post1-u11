package com.tienda.pedidos.service;

import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.entity.Pedido;
import com.tienda.pedidos.exception.RecursoNoEncontradoException;
import com.tienda.pedidos.repository.PedidoRepository;
import com.tienda.pedidos.strategy.EstrategiaDescuento;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquesta la creación y consulta de pedidos (SRP). Todas sus dependencias
 * llegan por constructor como abstracciones (DIP): ningún "new" de colaboradores.
 */
@Service
public class PedidoServiceImpl implements PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoServiceImpl.class);

    private static final double TASA_IVA = 0.19;
    private static final String TIPO_POR_DEFECTO = "REGULAR";
    private static final String NOTA_LOGISTICA = "Pedido creado automaticamente, revisar stock antes de despachar";

    private final PedidoRepository repo;
    private final NotificacionService notificador;
    private final Map<String, EstrategiaDescuento> estrategias;

    // Spring llena el mapa con todos los beans EstrategiaDescuento, usando el
    // nombre de cada bean (REGULAR, VIP, MAYORISTA) como llave
    public PedidoServiceImpl(PedidoRepository repo,
                             NotificacionService notificador,
                             Map<String, EstrategiaDescuento> estrategias) {
        this.repo = repo;
        this.notificador = notificador;
        this.estrategias = estrategias;
    }

    @Override
    @Transactional
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        log.info("Creando pedido: clienteEmail={}, tipoCliente={}", dto.getClienteEmail(), dto.getTipoCliente());
        String tipo = dto.getTipoCliente().trim().toUpperCase();
        if (!estrategias.containsKey(tipo)) {
            // Situación inusual pero recuperable: se aplica la estrategia por defecto
            log.warn("tipoCliente desconocido '{}' para clienteEmail={}; se aplica {}",
                    tipo, dto.getClienteEmail(), TIPO_POR_DEFECTO);
            tipo = TIPO_POR_DEFECTO;
        }
        EstrategiaDescuento estrategia = estrategias.get(tipo);
        double descuento = estrategia.calcularDescuento(dto.getSubtotal());
        // Único punto de auditoría del descuento (no se registra dentro de cada Strategy)
        log.info("Descuento aplicado: clienteEmail={}, tipoCliente={}, descuento={}",
                dto.getClienteEmail(), tipo, descuento);
        double baseConDescuento = dto.getSubtotal() - descuento;
        double iva = baseConDescuento * TASA_IVA;
        double total = baseConDescuento + iva;

        Pedido pedido = new Pedido();
        pedido.setClienteEmail(dto.getClienteEmail());
        pedido.setTipoCliente(tipo);
        pedido.setSubtotal(dto.getSubtotal());
        pedido.setDescuento(descuento);
        pedido.setIva(iva);
        pedido.setTotal(total);
        pedido.setEstado("CREADO");
        pedido.setNotasInternas(NOTA_LOGISTICA);
        pedido.setFechaCreacion(LocalDateTime.now());

        Pedido guardado = repo.save(pedido);
        // notasInternas NUNCA se registra: solo identificadores y montos
        log.info("Pedido creado exitosamente con id={}, total={}", guardado.getId(), guardado.getTotal());
        notificador.enviarConfirmacion(guardado.getId(), guardado.getClienteEmail(), guardado.getTotal());
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(Long id) {
        log.debug("Buscando pedido con id={}", id);
        Pedido p = repo.findById(id).orElseThrow(() -> {
            log.warn("Pedido con id={} no encontrado", id);
            return new RecursoNoEncontradoException("Pedido", id);
        });
        return toResponseDTO(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorCliente(String clienteEmail) {
        List<PedidoResponseDTO> pedidos = repo.findByClienteEmailOrderByIdAsc(clienteEmail).stream()
                .map(this::toResponseDTO).toList();
        log.debug("Pedidos encontrados para clienteEmail={}: {}", clienteEmail, pedidos.size());
        return pedidos;
    }

    // Mapeo explícito entidad → DTO: notasInternas se queda fuera a propósito
    private PedidoResponseDTO toResponseDTO(Pedido p) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(p.getId());
        dto.setClienteEmail(p.getClienteEmail());
        dto.setTipoCliente(p.getTipoCliente());
        dto.setSubtotal(p.getSubtotal());
        dto.setDescuento(p.getDescuento());
        dto.setIva(p.getIva());
        dto.setTotal(p.getTotal());
        dto.setEstado(p.getEstado());
        dto.setFechaCreacion(p.getFechaCreacion());
        return dto;
    }
}
