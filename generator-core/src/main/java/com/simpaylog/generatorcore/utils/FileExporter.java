package com.simpaylog.generatorcore.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorcore.dto.TransactionLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FileExporter {

    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public byte[] toCSV(List<TransactionLog> transactions) throws IOException {
        try (StringWriter writer = new StringWriter();
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("uuid", "userId", "timestamp", "transactionType", "description", "amount", "balanceBefore", "balanceAfter"))) {

            for (TransactionLog t : transactions) {
                csvPrinter.printRecord(
                        t.uuid(),
                        t.userId(),
                        DATE_FORMATTER.format(t.timestamp()),
                        t.transactionType().name(),
                        t.description(),
                        t.amount(),
                        t.balanceBefore(),
                        t.balanceAfter()
                );
            }
            csvPrinter.flush();
            return writer.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    public byte[] toJSON(List<TransactionLog> transactions) throws IOException {
        return objectMapper.writeValueAsBytes(transactions);
    }
}