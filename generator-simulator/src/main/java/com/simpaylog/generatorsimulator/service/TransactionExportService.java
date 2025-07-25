package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorsimulator.dto.TransactionLog;
import com.simpaylog.generatorsimulator.repository.ESTransactionSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionExportService {

    private final ESTransactionSearchRepository repository;

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