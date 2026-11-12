package com.tienda.pedidos.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Cuerpo JSON uniforme para todas las respuestas de error de la API. */
@Schema(description = "Respuesta de error estándar de la API")
public class ApiError {

    @Schema(description = "Código HTTP", example = "404")
    private final int status;

    @Schema(description = "Nombre del error HTTP", example = "Not Found")
    private final String error;

    @Schema(description = "Mensaje para el cliente", example = "Pedido con id 999 no encontrado.")
    private final String mensaje;

    @Schema(description = "Momento del error (ISO-8601)", example = "2026-11-03T10:15:30")
    private final String timestamp;

    @Schema(description = "Ruta solicitada", example = "/api/pedidos/999")
    private final String path;

    public ApiError(int status, String error, String mensaje, String path) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.path = path;
        this.timestamp = LocalDateTime.now().toString();
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMensaje() { return mensaje; }
    public String getTimestamp() { return timestamp; }
    public String getPath() { return path; }
}
