package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorcore.repository.ElasticsearchRepository;
import com.simpaylog.generatorcore.utils.FileExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.OutputStream;

@Service
@RequiredArgsConstructor
public class TransactionExportService {

    private final ElasticsearchRepository repository;
    private final FileExporter fileExporter;

    public void exportAllTransactions(String format, OutputStream outputStream) {
        if (format.equalsIgnoreCase("csv")) {
            fileExporter.writeCsv(outputStream, repository::findAllTransactionsForExport);
        } else if (format.equalsIgnoreCase("json")) {
            fileExporter.writeJson(outputStream, repository::findAllTransactionsForExport);
        } else {
            throw new IllegalArgumentException("지원하지 않는 포맷: " + format);
        }
    }
}

