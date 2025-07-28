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

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionExportService transactionExportService;

    @GetMapping
    public ResponseEntity<byte[]> exportTransactions(@RequestParam String format) {
        byte[] data = transactionExportService.exportAllTransaction(format);

        String contentType = switch (format.toLowerCase()) {
            case "csv" -> "text/csv";
            case "json" -> "application/json";
            default -> throw new IllegalArgumentException("파일 변환을 지원하지 않는 형식입니다.: " + format);
        };

        String fileName = "transactions." + format;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(data);
    }
}
