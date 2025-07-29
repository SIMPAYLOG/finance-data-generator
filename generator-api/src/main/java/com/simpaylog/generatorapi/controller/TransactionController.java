package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.service.TransactionExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionExportService transactionExportService;

    @GetMapping
    public ResponseEntity<StreamingResponseBody> exportTransactions(@RequestParam String format) {
        StreamingResponseBody stream = outputStream -> {
            transactionExportService.exportAllTransactions(format, outputStream);
        };

        String contentType = switch (format.toLowerCase()) {
            case "csv" -> "text/csv";
            case "json" -> "application/json";
            default -> throw new IllegalArgumentException("지원하지 않는 형식: " + format);
        };

        String fileName = "transactions." + format;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(stream);
    }

}
