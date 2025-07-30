package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.service.TransactionExportService;
import com.simpaylog.generatorcore.enums.export.ExportFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Arrays;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionExportService transactionExportService;

    @GetMapping
    public ResponseEntity<StreamingResponseBody> exportTransactions(
            @RequestParam String format,
            @RequestParam String sessionId
    ) {
        // 문자열 format을 ExportFormat Enum으로 변환
        ExportFormat exportFormat = ExportFormat.fromString(format)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "지원하지 않는 형식: " + format + ". 유효한 형식은 " + Arrays.toString(ExportFormat.values()) + " 입니다."
                ));

        StreamingResponseBody stream = outputStream -> {
            // Enum 상수를 서비스 계층으로 전달
            transactionExportService.exportAllTransactions(exportFormat, sessionId, outputStream);
        };

        String fileName = "transactions." + exportFormat.getValue(); // Enum의 value 사용

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exportFormat.getMimeType())) // Enum의 mimeType 사용
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(stream);
    }

}
