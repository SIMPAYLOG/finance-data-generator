package com.simpaylog.generatorcore.dto.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(indexName = "transaction-logs")
@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionLogDocument(
        @Id
        String uuid,
        @Field(type = FieldType.Long) // 명시적으로 필드 타입 지정
        Long userId,
        @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        @Field(type = FieldType.Keyword) // Enum이나 정확한 매칭을 위해 Keyword 타입으로
        TransactionType transactionType,
        @Field(type = FieldType.Text) // 전문 검색용 text 타입
        String description,
        @Field(type = FieldType.Double) // 금액은 실수 타입으로
        BigDecimal amount,
        @Field(type = FieldType.Double)
        BigDecimal balanceBefore,
        @Field(type = FieldType.Double)
        BigDecimal balanceAfter
) {
    public enum TransactionType {
        WITHDRAW,
        DEPOSIT
    }

    public static TransactionLogDocument of(Long userId, LocalDateTime timestamp, TransactionType transactionType,String description, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return new TransactionLogDocument(
                UUID.randomUUID().toString(),
                userId,
                timestamp,
                transactionType,
                description,
                amount,
                balanceBefore,
                balanceAfter
        );
    }

}
