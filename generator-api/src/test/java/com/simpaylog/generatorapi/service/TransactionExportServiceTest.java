package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.TestConfig;
import com.simpaylog.generatorapi.exception.ApiException;
import com.simpaylog.generatorcore.dto.Document.TransactionLogDocument;
import com.simpaylog.generatorcore.enums.export.ExportFormat;
import com.simpaylog.generatorcore.repository.Elasticsearch.ElasticsearchRepository;
import com.simpaylog.generatorcore.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {
        "spring.kafka.topic.transaction=topic-transaction",
        "spring.kafka.topic.tx.request=dummy-topic",
        "spring.kafka.topic.tx.response=dummy-topic",
        "spring.kafka.consumer.group-id=test-group"
})
@Import(TransactionExportService.class)
public class TransactionExportServiceTest extends TestConfig {
    @Autowired
    TransactionExportService transactionExportService;
    @MockitoBean
    ElasticsearchRepository repository;
    @MockitoBean
    UserService userService;

    @Test
    public void exportAllTransactions_csv_정상_동작한다() {
        // given
        String sessionId = "sessionId1";
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TransactionLogDocument doc = new TransactionLogDocument(
                "uuid1", 1001L, LocalDateTime.now(),
                TransactionLogDocument.TransactionType.DEPOSIT, "급여",
                new BigDecimal("10000"), new BigDecimal("0"), new BigDecimal("10000")
        );

        doAnswer(invocation -> {
            Consumer<TransactionLogDocument> consumer = invocation.getArgument(1);
            consumer.accept(doc);
            return null;
        }).when(repository).findAllTransactionsForExport(eq(sessionId), any());

        // when
        transactionExportService.exportAllTransactions(ExportFormat.CSV, sessionId, out);

        // then
        String result = out.toString(StandardCharsets.UTF_8);
        assertThat(result).contains("uuid1", "급여", "10000");
    }

    @Test
    public void exportAllTransactions_json_정상_동작한다() {
        // given
        String sessionId = "sessionId2";
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TransactionLogDocument doc = new TransactionLogDocument(
                "uuid2", 2002L, LocalDateTime.of(2025, 7, 2, 9, 0),
                TransactionLogDocument.TransactionType.WITHDRAW, "현금 인출",
                new BigDecimal("5000"), new BigDecimal("10000"), new BigDecimal("5000")
        );

        doAnswer(invocation -> {
            Consumer<TransactionLogDocument> consumer = invocation.getArgument(1);
            consumer.accept(doc);
            return null;
        }).when(repository).findAllTransactionsForExport(eq(sessionId), any());

        // when
        transactionExportService.exportAllTransactions(ExportFormat.JSON, sessionId, out);

        // then
        String result = out.toString(StandardCharsets.UTF_8);
        assertThat(result).contains("\"uuid\":\"uuid2\"", "\"description\":\"현금 인출\"");
    }

    @Test
    public void exportAllTransactions_데이터가_없으면_CSV_헤더만_반환된다() {
        // given
        String sessionId = "sessionId";
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        doAnswer(invocation -> null).when(repository).findAllTransactionsForExport(eq(sessionId), any());

        // when
        transactionExportService.exportAllTransactions(ExportFormat.CSV, sessionId, out);

        // then
        String result = out.toString(StandardCharsets.UTF_8);
        long lineCount = result.lines().count();
        assertThat(lineCount).isEqualTo(1); // CSV 헤더만 존재
    }

    @Test
    public void exportAllTransactions_데이터가_없으면_JSON_빈배열_반환한다() {
        // given
        String sessionId = "sessionId";
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        doAnswer(invocation -> null).when(repository).findAllTransactionsForExport(eq(sessionId), any());

        // when
        transactionExportService.exportAllTransactions(ExportFormat.JSON, sessionId, out);

        // then
        String result = out.toString(StandardCharsets.UTF_8);
        assertThat(result.trim()).isEqualTo("[]");
    }

    @Test
    public void exportAllTransactions_description이_null이면_예외발생한다() {
        // given
        String sessionId = "sessionId";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TransactionLogDocument invalidDoc = new TransactionLogDocument(
                "uuid3", 3003L, LocalDateTime.now(),
                TransactionLogDocument.TransactionType.DEPOSIT, null,
                new BigDecimal("5000"), new BigDecimal("1000"), new BigDecimal("6000")
        );

        doAnswer(invocation -> {
            Consumer<TransactionLogDocument> consumer = invocation.getArgument(1);
            consumer.accept(invalidDoc);
            return null;
        }).when(repository).findAllTransactionsForExport(eq(sessionId), any());

        // when + then
        assertThatThrownBy(() ->
                transactionExportService.exportAllTransactions(ExportFormat.CSV, sessionId, out)
        ).isInstanceOf(ApiException.class)
                .hasMessageContaining("파일 처리 중 오류가 발생했습니다");
    }
}