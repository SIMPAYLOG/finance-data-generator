package com.simpaylog.generatorapi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorapi.dto.document.TransactionLogDocument;
import com.simpaylog.generatorapi.dto.request.ExportRequest;
import com.simpaylog.generatorcore.enums.export.TransactionCsvExportHeader;
import com.simpaylog.generatorcore.exception.CoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileExporter {

    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter CSV_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void writeCsv(ExportRequest request, OutputStream os, Consumer<Consumer<TransactionLogDocument>> fetcher) {
        int[] counter = {0};
        try (OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(osw, CSVFormat.DEFAULT)) {

            // request.columns 기반으로 헤더 선택
            var selectedHeaders = java.util.Arrays.stream(TransactionCsvExportHeader.values())
                    .filter(h -> request.columns().contains(h.getFieldName()))
                    .toList();

            // CSV 헤더 출력
            printer.printRecord(selectedHeaders.stream()
                    .map(TransactionCsvExportHeader::getDisplayName)
                    .toArray(String[]::new));

            // 동적 Consumer 생성
            Consumer<TransactionLogDocument> recordConsumer = createCsvConsumer(printer, counter, request);
            fetcher.accept(recordConsumer);

            printer.flush();
            log.info("CSV로 저장된 총 데이터 건수: {}", counter[0]);
        } catch (IOException e) {
            throw new CoreException("CSV 파일 쓰기 중 오류 발생");
        }
    }


    public void writeJson(OutputStream os, Consumer<Consumer<TransactionLogDocument>> fetcher) {
        int[] counter = {0};
        try (OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            osw.write("[");
            final boolean[] first = {true};

            Consumer<TransactionLogDocument> recordConsumer = createJsonConsumer(osw, objectMapper, counter, first);
            fetcher.accept(recordConsumer);

            osw.write("]");
            osw.flush();

            log.info("JSON으로 저장된 총 데이터 건수: {}", counter[0]);
        } catch (IOException e) {
            throw new CoreException("JSON 파일 쓰기 중 오류 발생");
        }
    }

    private Consumer<TransactionLogDocument> createCsvConsumer(CSVPrinter printer, int[] counter, ExportRequest request) {
        // request.columns 기반으로 출력할 컬럼 enum 선택
        var selectedHeaders = java.util.Arrays.stream(TransactionCsvExportHeader.values())
                .filter(h -> request.columns().contains(h.getFieldName()))
                .toList();

        return t -> {
            try {
                validateTransactionLog(t);

                var values = selectedHeaders.stream()
                        .map(h -> getFieldValue(t, h.getFieldName()))
                        .toArray();

                printer.printRecord(values);
                counter[0]++;
                if (counter[0] % 1000 == 0) {
                    printer.flush();
                }
            } catch (IOException e) {
                throw new CoreException("CSV 레코드 쓰기 중 오류 발생");
            }
        };
    }

    private Consumer<TransactionLogDocument> createJsonConsumer(OutputStreamWriter osw, ObjectMapper mapper, int[] counter, boolean[] first) {
        return t -> {
            try {
                validateTransactionLog(t);

                if (!first[0]) {
                    osw.write(",");
                } else {
                    first[0] = false;
                }
                String json = mapper.writeValueAsString(t);
                osw.write(json);
                counter[0]++;
                if (counter[0] % 1000 == 0) {
                    osw.flush();
                }
            } catch (IOException e) {
                throw new CoreException("JSON 레코드 쓰기 중 오류 발생");
            }
        };
    }

    public String[] getHeaderValues() {
        return java.util.Arrays.stream(TransactionCsvExportHeader.values())
                .map(TransactionCsvExportHeader::getDisplayName)
                .toArray(String[]::new);
    }

    private void validateTransactionLog(TransactionLogDocument t) {
        if (t.transactionId() == null ||
                t.userId() == null ||
                t.timestamp() == null ||
                t.transactionType() == null ||
                t.description() == null ||
                t.amount() == null) {
            throw new CoreException("TransactionLogDocument 필드 중 null 값이 존재합니다");
        }
    }

    /**
     * TransactionLogDocument 필드값 또는 집계 컬럼 값을 가져오는 메서드
     */
    private Object getFieldValue(TransactionLogDocument t, String fieldName) {
        return switch (fieldName) {
            case "transactionId" -> t.transactionId();
            case "userId" -> t.userId();
            case "timestamp" -> t.timestamp().format(CSV_DATE_FORMATTER);
            case "transactionType" -> t.transactionType().name();
            case "detailType" -> t.detailType();
            case "category" -> t.category();
            case "subcategory" -> t.subcategory();
            case "counterparty" -> t.counterparty();
            case "channel" -> t.channel();
            case "balanceBefore" -> t.balanceBefore() != null ? t.balanceBefore().toPlainString() : "";
            case "balanceAfter" -> t.balanceAfter() != null ? t.balanceAfter().toPlainString() : "";
            case "description" -> t.description();
            case "memo" -> t.memo();
            case "amount" -> t.amount() != null ? t.amount().toPlainString() : "";
            // 집계 컬럼은 나중에 DTO로 받을 경우 처리
            case "totalSpent", "avgTransaction", "categoryRatios", "incomeVsSpending" -> "";
            default -> "";
        };
    }
}
