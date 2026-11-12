package com.tienda.pedidos.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Manejo centralizado de errores: reemplaza el try/catch disperso del código de
 * partida (que devolvía null con 200 OK). Cada excepción se traduce a su código
 * HTTP con un cuerpo ApiError uniforme.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return new ApiError(404, "Not Found", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        log.warn("Petición inválida en {}: {}", req.getRequestURI(), errores);
        return new ApiError(400, "Bad Request", errores, req.getRequestURI());
    }

    // JSON mal formado o tipos incorrectos (ej. "subtotal": "abc") → 400, no 500
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleJsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.warn("Cuerpo JSON ilegible en {}", req.getRequestURI());
        return new ApiError(400, "Bad Request", "El cuerpo de la petición no es un JSON válido.", req.getRequestURI());
    }

    // Parámetro de consulta obligatorio ausente (GET /api/pedidos sin clienteEmail) → 400, no 500
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleParametroFaltante(MissingServletRequestParameterException ex, HttpServletRequest req) {
        log.warn("Falta el parámetro {} en {}", ex.getParameterName(), req.getRequestURI());
        return new ApiError(400, "Bad Request",
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'.", req.getRequestURI());
    }

    // Tipo incorrecto en la ruta o la consulta (GET /api/pedidos/abc) → 400, no 500
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        log.warn("Valor inválido para {} en {}", ex.getName(), req.getRequestURI());
        return new ApiError(400, "Bad Request",
                "El valor de '" + ex.getName() + "' no tiene el tipo esperado.", req.getRequestURI());
    }

    // Último recurso: el detalle técnico va al log (ERROR) y NUNCA al cliente
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneral(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado procesando {}", req.getRequestURI(), ex);
        return new ApiError(500, "Internal Server Error",
                "Error inesperado. Contactar soporte.", req.getRequestURI());
    }
}
