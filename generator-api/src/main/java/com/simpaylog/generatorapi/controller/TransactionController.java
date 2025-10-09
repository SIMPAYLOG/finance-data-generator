package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.dto.request.ExportRequest;
import com.simpaylog.generatorapi.service.TransactionExportService;
import com.simpaylog.generatorcore.enums.export.ExportFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionExportService transactionExportService;

    @PostMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportTransactions(@RequestBody ExportRequest request) {
        StreamingResponseBody stream = transactionExportService.getExportStreamingBody(request);

        String fileName = "transactions." + request.format().toLowerCase(); // 파일 확장자 추정
        String mimeType = ExportFormat.fromString(request.format())
                .map(ExportFormat::getMimeType)
                .orElse("application/octet-stream"); // fallback

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(stream);
    }

}
