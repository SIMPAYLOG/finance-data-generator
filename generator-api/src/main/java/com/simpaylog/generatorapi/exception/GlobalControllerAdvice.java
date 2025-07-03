package com.simpaylog.generatorapi.exception;

import com.simpaylog.generatorapi.dto.response.Response;
import com.simpaylog.generatorapi.dto.response.Status;
import com.simpaylog.generatorcore.exception.CoreException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(ApiException.class)
    public Response<Void> handleApiError(ApiException e) {
        log.error("Error occur: {}", e.toString());
        return Response.error(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Status> handleValidationError(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return Response.clientError(String.valueOf(errors));
        //return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<Status> handleCoreError(CoreException ex) {
        return Response.clientError(ex.getMessage());
    }

}
