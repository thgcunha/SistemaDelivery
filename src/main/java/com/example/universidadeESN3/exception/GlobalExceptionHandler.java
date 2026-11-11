package com.example.universidadeESN3.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String INTERNAL_ERROR_MESSAGE =
            "Ocorreu um erro interno. Tente novamente mais tarde.";

    @ExceptionHandler({RestauranteNotFoundException.class, PratoNotFoundException.class,
            PedidoNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(
            RuntimeException ex, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErrorResponse> handleBusinessConflict(
            RegraNegocioException ex, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(
            ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        return response(status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status,
                ex.getReason() == null ? "Não foi possível processar a requisição." : ex.getReason(), request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? "O parâmetro '" + mismatch.getName() + "' possui um valor inválido."
                : "A requisição contém um valor de tipo inválido.";
        return mvcResponse(ex, headers, status, message, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        return mvcResponse(ex, headers, status,
                "O parâmetro '" + ex.getParameterName() + "' é obrigatório.", request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        return mvcResponse(ex, headers, status,
                "O corpo da requisição está ausente ou possui formato inválido.", request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Erro de validação");
        return mvcResponse(ex, headers, HttpStatus.UNPROCESSABLE_ENTITY, message, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("Falha ao processar {}", request.getDescription(false), ex);
        }

        ErrorResponse error = body instanceof ErrorResponse response
                ? response : createError(status, defaultMessage(status), path(request));
        return super.handleExceptionInternal(ex, error, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE, request);
    }

    private ResponseEntity<Object> mvcResponse(Exception ex, HttpHeaders headers, HttpStatusCode status,
                                               String message, WebRequest request) {
        return handleExceptionInternal(ex, createError(status, message, path(request)),
                headers, status, request);
    }

    private ResponseEntity<ErrorResponse> response(
            HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(createError(status, message, request.getRequestURI()));
    }

    private ErrorResponse createError(HttpStatusCode status, String message, String path) {
        HttpStatus httpStatus = HttpStatus.resolve(status.value());
        String error = httpStatus == null ? "HTTP Error" : httpStatus.getReasonPhrase();
        if (httpStatus == HttpStatus.UNPROCESSABLE_ENTITY) {
            error = "Validation Error";
        }
        return new ErrorResponse(status.value(), error, message, path, LocalDateTime.now());
    }

    private String path(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }

    private String defaultMessage(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Requisição inválida.";
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método HTTP não permitido para este recurso.";
            case 406 -> "Formato de resposta não suportado.";
            case 415 -> "Tipo de conteúdo não suportado.";
            default -> status.is5xxServerError()
                    ? INTERNAL_ERROR_MESSAGE : "Não foi possível processar a requisição.";
        };
    }
}
