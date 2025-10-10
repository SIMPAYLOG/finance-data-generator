package com.simpaylog.generatorapi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorapi.dto.document.AggregatedTransactionDocument;
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
            printer.printRecord(
                    selectedHeaders.stream()
                            .map(TransactionCsvExportHeader::getDisplayName)
                            .toList()
            );


            // 동적 Consumer 생성
            Consumer<TransactionLogDocument> recordConsumer = createCsvConsumer(printer, counter, request);
            fetcher.accept(recordConsumer);

            printer.flush();
            log.info("CSV로 저장된 총 데이터 건수: {}", counter[0]);
        } catch (IOException e) {
            throw new CoreException("CSV 파일 쓰기 중 오류 발생");
        }
    }

    public void writeCsvForAggregated(ExportRequest request, OutputStream os, Consumer<Consumer<AggregatedTransactionDocument>> fetcher) {
        int[] counter = {0};
        try (OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(osw, CSVFormat.DEFAULT)) {

            // 요청된 컬럼 필터링
            var selectedHeaders = java.util.Arrays.stream(TransactionCsvExportHeader.values())
                    .filter(h -> request.columns().contains(h.getFieldName()))
                    .toList();

            printer.printRecord(
                    selectedHeaders.stream()
                            .map(TransactionCsvExportHeader::getDisplayName)
                            .toList()
            );

            Consumer<AggregatedTransactionDocument> recordConsumer = dto -> {
                try {
                    var values = selectedHeaders.stream()
                            .map(h -> getAggregatedFieldValue(dto, h.getFieldName()))
                            .toArray();

                    printer.printRecord(values);
                    counter[0]++;
                    if (counter[0] % 1000 == 0) printer.flush();
                } catch (IOException e) {
                    throw new CoreException("CSV 레코드 쓰기 중 오류 발생");
                }
            };

            fetcher.accept(recordConsumer);
            printer.flush();
            log.info("집계 CSV로 저장된 총 건수: {}", counter[0]);
        } catch (IOException e) {
            throw new CoreException("집계 CSV 파일 쓰기 중 오류 발생");
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
                        .map(h -> getFieldValue(t, h.getFieldName(), request.isMasked()))
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
    private Object getFieldValue(TransactionLogDocument t, String fieldName, boolean isMasked) {
        return switch (fieldName) {
            case "transactionId" -> {
                String transactionIdStr = "TX-" + t.transactionId();
                yield isMasked ? masking(transactionIdStr, 4, 4) : transactionIdStr;
            }
            case "userId" -> {
                String userIdStr = "U-" + t.userId();
                yield isMasked ? masking(userIdStr, 2, 2) : userIdStr;
            }
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
            default -> "";
        };
    }

    private Object getAggregatedFieldValue(AggregatedTransactionDocument t, String fieldName) {
        return switch (fieldName) {
            case "userId" -> "U-" + t.userId();
            case "period" -> t.period();
            case "totalSpent" -> t.totalSpent();
            case "avgTxn" -> t.avgTxn();
            case "top3Categories" -> String.join(", ", t.top3Categories());
            case "foodRatio" -> String.format("%.3f", t.foodRatio());
            case "transportRatio" -> String.format("%.3f", t.transportRatio());
            case "leisureRatio" -> String.format("%.3f", t.leisureRatio());
            case "groceriesNonAlcoholicBeveragesRatio" -> String.format("%.3f", t.groceriesNonAlcoholicBeveragesRatio());
            case "alcoholicBeveragesTobaccoRatio" -> String.format("%.3f", t.alcoholicBeveragesTobaccoRatio());
            case "clothingFootwearRatio" -> String.format("%.3f", t.clothingFootwearRatio());
            case "housingUtilitiesFuelRatio" -> String.format("%.3f", t.housingUtilitiesFuelRatio());
            case "householdGoodsServicesRatio" -> String.format("%.3f", t.householdGoodsServicesRatio());
            case "healthRatio" -> String.format("%.3f", t.healthRatio());
            case "communicationRatio" -> String.format("%.3f", t.communicationRatio());
            case "educationRatio" -> String.format("%.3f", t.educationRatio());
            case "otherGoodsServicesRatio" -> String.format("%.3f", t.otherGoodsServicesRatio());
            case "incomeVsSpending" -> String.format("%.3f", t.incomeVsSpending());
            default -> "";
        };
    }

    //데이터 마스킹 메서드
    private String masking(String id, int prefixLen, int suffixLen) {
        if (id == null || id.isEmpty()) return id;

        int length = id.length();
        if(length == 3) suffixLen = 0;
        else if(length == 4) suffixLen = 1;
        int maskLen = length - prefixLen - suffixLen;

        // 최소 1글자는 마스킹
        if (maskLen < 1) {
            maskLen = 1;
            // prefixLen과 suffixLen 재조정
            if (length <= 2) {
                prefixLen = 1;
                suffixLen = 0;
            } else if (length == 3) {
                prefixLen = 1;
                suffixLen = 1;
            } else {
                prefixLen = Math.max(1, length - suffixLen - maskLen);
            }
        }

        String prefix = id.substring(0, Math.min(prefixLen, length));
        String suffix = length > prefixLen + maskLen ? id.substring(length - suffixLen) : "";
        String masked = "*".repeat(maskLen);

        return prefix + masked + suffix;
    }
}
