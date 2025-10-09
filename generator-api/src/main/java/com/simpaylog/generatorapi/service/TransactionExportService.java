package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.dto.request.ExportRequest;
import com.simpaylog.generatorapi.exception.ApiException;
import com.simpaylog.generatorapi.exception.ErrorCode;
import com.simpaylog.generatorapi.repository.Elasticsearch.TransactionAggregationRepository;
import com.simpaylog.generatorcore.enums.export.ExportFormat;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorapi.utils.FileExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;

import static com.simpaylog.generatorapi.exception.ErrorCode.INVALID_EXPORT_FORMAT;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionExportService {

    private final TransactionAggregationRepository repository;
    private final FileExporter fileExporter;

    // TODO: sessionId 없는 경우 예외처리 필요
    public StreamingResponseBody getExportStreamingBody(ExportRequest request) {
        ExportFormat exportFormat = ExportFormat.fromString(request.format())
                .orElseThrow(() -> new ApiException(INVALID_EXPORT_FORMAT));

        return outputStream -> exportTransactions(exportFormat, request, outputStream);
    }

    public void exportTransactions(ExportFormat format, ExportRequest request, OutputStream outputStream) {
        try {
            switch (format) {
                case CSV:
                    fileExporter.writeCsv(request, outputStream, consumer -> repository.findTransactionsForExport(request, consumer));
                    break;
                case JSON:
                    fileExporter.writeJson(outputStream, consumer -> repository.findTransactionsForExport(request, consumer));
                    break;
            }
        } catch (CoreException e) {
            log.error(e.getMessage());
            System.out.println("Error: " + e.getMessage());
            throw new ApiException(ErrorCode.FILE_WRITE_ERROR);
        }
    }
}

