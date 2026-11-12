package com.tienda.pedidos.controller;

import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.exception.ApiError;
import com.tienda.pedidos.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Solo traduce HTTP ↔ DTO y delega en PedidoService: cero lógica de negocio. */
@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Creación y consulta de pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @Operation(summary = "Crear un nuevo pedido",
            description = "Calcula el descuento según tipoCliente, aplica IVA del 19 % y devuelve el pedido creado.")
    @ApiResponse(responseCode = "201", description = "Pedido creado, con descuento aplicado según tipoCliente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
            content = @Content(schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"Bad Request\", "
                            + "\"mensaje\": \"clienteEmail: El email del cliente es obligatorio\", "
                            + "\"timestamp\": \"2026-11-03T10:15:30\", \"path\": \"/api/pedidos\"}")))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponseDTO crear(@Valid @RequestBody PedidoRequestDTO dto) {
        return service.crear(dto);
    }

    @Operation(summary = "Obtener pedido por ID")
    @ApiResponse(responseCode = "200", description = "Pedido encontrado")
    @ApiResponse(responseCode = "400", description = "El id no es numérico",
            content = @Content(schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"Bad Request\", "
                            + "\"mensaje\": \"El valor de 'id' no tiene el tipo esperado.\", "
                            + "\"timestamp\": \"2026-11-03T10:15:30\", \"path\": \"/api/pedidos/abc\"}")))
    @ApiResponse(responseCode = "404", description = "Pedido no encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{id}")
    public PedidoResponseDTO obtener(
            @Parameter(description = "ID del pedido", example = "1") @PathVariable Long id) {
        return service.buscarPorId(id); // sin try/catch: la excepción sube al GlobalExceptionHandler
    }

    @Operation(summary = "Listar los pedidos de un cliente",
            description = "Devuelve los pedidos del correo indicado ordenados por id (lista vacía si no tiene).")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos del cliente")
    @ApiResponse(responseCode = "400", description = "Falta el parámetro clienteEmail",
            content = @Content(schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"Bad Request\", "
                            + "\"mensaje\": \"Falta el parámetro obligatorio 'clienteEmail'.\", "
                            + "\"timestamp\": \"2026-11-03T10:15:30\", \"path\": \"/api/pedidos\"}")))
    @GetMapping
    public List<PedidoResponseDTO> listarPorCliente(
            @Parameter(description = "Correo del cliente", example = "ana@correo.com")
            @RequestParam String clienteEmail) {
        return service.listarPorCliente(clienteEmail);
    }
}
