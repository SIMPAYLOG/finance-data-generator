package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorcore.dto.TransactionLog;
import com.simpaylog.generatorcore.repository.ESTransactionSearchRepository;
import com.simpaylog.generatorcore.utils.FileExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionExportService {

    private final ESTransactionSearchRepository repository;
    private final FileExporter fileExporter;

    public byte[] exportAllTransaction(String format) {
        List<TransactionLog> allTransactions = getAllTransaction();
        if (allTransactions == null || allTransactions.isEmpty()) {
            return new byte[0]; // 또는 예외 처리
        }

        try {
            return switch (format.toLowerCase()) {
                case "csv" -> fileExporter.toCSV(allTransactions);
                case "json" -> fileExporter.toJSON(allTransactions);
                default -> throw new IllegalArgumentException("파일 변환을 지원하지 않는 형식입니다.: " + format);
            };
        } catch (IOException e) {
            throw new RuntimeException("데이터 변환중 에러가 발생했습니다.", e);
        }
    }

    public List<TransactionLog> getAllTransaction() {
        int page = 0;
        int size = 1000;

        List<TransactionLog> allTransactions = new ArrayList<>();

        try{
            while (true) {
                Page<TransactionLog> transactionPage = repository.findAllByOrderByUserIdAscTimestampAsc(PageRequest.of(page, size));
                if (transactionPage.isEmpty()) break;

                allTransactions.addAll(transactionPage.getContent());

                if (transactionPage.isLast()) break;
                page++;
            }
        } catch (Exception e){
            System.out.println("Exception in TransactionExportService : " + e.getMessage());
            allTransactions = null;
        }

        return allTransactions;
    }
}