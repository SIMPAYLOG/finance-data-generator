package com.simpaylog.generatorapi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorapi.dto.document.TransactionLogDocument;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class FileExporter {

    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter CSV_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void writeCsv(OutputStream os, Consumer<Consumer<TransactionLogDocument>> fetcher) {
        int[] counter = {0};
        try (OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(osw, CSVFormat.DEFAULT)) {

            printer.printRecord((Object[]) getHeaderValues());

            Consumer<TransactionLogDocument> recordConsumer = createCsvConsumer(printer, counter);
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

    private Consumer<TransactionLogDocument> createCsvConsumer(CSVPrinter printer, int[] counter) {
        return t -> {
            try {
                validateTransactionLog(t);

                printer.printRecord(
                        t.uuid(),
                        t.userId(),
                        t.timestamp().format(CSV_DATE_FORMATTER),
                        t.transactionType().name(),
                        t.description(),
                        t.amount().toPlainString(),
                        t.balanceBefore().toPlainString(),
                        t.balanceAfter().toPlainString()
                );
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
        if (t.uuid() == null ||
                t.userId() == null ||
                t.timestamp() == null ||
                t.transactionType() == null ||
                t.description() == null ||
                t.amount() == null ||
                t.balanceBefore() == null ||
                t.balanceAfter() == null) {
            throw new CoreException("TransactionLogDocument 필드 중 null 값이 존재합니다");
        }
    }
}
