package com.simpaylog.generatorapi.service;

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
    public StreamingResponseBody getExportStreamingBody(String format, String sessionId) {
        ExportFormat exportFormat = ExportFormat.fromString(format)
                .orElseThrow(() -> new ApiException(INVALID_EXPORT_FORMAT));

        return outputStream -> exportAllTransactions(exportFormat, sessionId, outputStream);
    }

    public void exportAllTransactions(ExportFormat format, String sessionId, OutputStream outputStream) {
        try {
            switch (format) {
                case CSV:
                    fileExporter.writeCsv(outputStream, consumer -> repository.findAllTransactionsForExport(sessionId, consumer));
                    break;
                case JSON:
                    fileExporter.writeJson(outputStream, consumer -> repository.findAllTransactionsForExport(sessionId, consumer));
                    break;
            }
        } catch (CoreException e) {
            log.error(e.getMessage());
            throw new ApiException(ErrorCode.FILE_WRITE_ERROR);
        }
    }
}

