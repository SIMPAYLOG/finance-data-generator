package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.exception.ApiException;
import com.simpaylog.generatorapi.exception.ErrorCode;
import com.simpaylog.generatorcore.enums.export.ExportFormat;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.Elasticsearch.ElasticsearchRepository;
import com.simpaylog.generatorcore.utils.FileExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.OutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionExportService {

    private final ElasticsearchRepository repository;
    private final FileExporter fileExporter;

    public void exportAllTransactions(ExportFormat format, OutputStream outputStream) {
        try {
            switch (format) {
                case CSV:
                    fileExporter.writeCsv(outputStream, repository::findAllTransactionsForExport);
                    break;
                case JSON:
                    fileExporter.writeJson(outputStream, repository::findAllTransactionsForExport);
                    break;
            }
        } catch (CoreException e) {
            log.error(e.getMessage());
            throw new ApiException(ErrorCode.FILE_WRITE_ERROR);
        }
    }
}

