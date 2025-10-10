package com.simpaylog.generatorapi.dto.document;

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
        String transactionId,

        String userId,

        @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,

        @Field(type = FieldType.Keyword)
        TransactionType transactionType,

        @Field(type = FieldType.Double)
        BigDecimal amount,

        @Field(type = FieldType.Keyword)
        String detailType,

        @Field(type = FieldType.Keyword)
        String category,

        @Field(type = FieldType.Keyword)
        String subcategory,

        @Field(type = FieldType.Keyword)
        String counterparty,

        @Field(type = FieldType.Keyword)
        String channel,

        @Field(type = FieldType.Double)
        BigDecimal balanceBefore,

        @Field(type = FieldType.Double)
        BigDecimal balanceAfter,

        @Field(type = FieldType.Text)
        String description,

        @Field(type = FieldType.Text)
        String memo
) {

    public enum TransactionType {
        WITHDRAW,
        DEPOSIT
    }
}

