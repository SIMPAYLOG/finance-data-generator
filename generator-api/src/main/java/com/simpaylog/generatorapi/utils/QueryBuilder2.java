package com.simpaylog.generatorapi.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QueryBuilder2 {

    public static String hourAggregationQuery(String sessionId, LocalDate from, LocalDate to) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);
        String gte = start.format(formatter);
        String lte = end.format(formatter);

        return """
                {
                  "size": 0,
                  "query": {
                    "bool": {
                      "must": [
                        {
                          "range": {
                            "timestamp": {
                              "gte": "%s",
                              "lte": "%s",
                              "time_zone": "Asia/Seoul"
                            }
                          }
                        },
                        {
                          "term": {
                            "sessionId": "%s"
                          }
                        }
                      ]
                    }
                  },
                  "aggs": {
                    "by_transaction_type": {
                      "terms": {
                        "field": "transactionType",
                        "size": 2
                      },
                      "aggs": {
                        "by_hour": {
                          "terms": {
                            "script": {
                              "lang": "painless",
                              "source": "doc['timestamp'].value.getHour()"
                            },
                            "size": 24,
                            "order": {
                              "_key": "asc"
                            }
                          },
                          "aggs": {
                            "transaction_count": {
                              "value_count": {
                                "field": "uuid"
                              }
                            },
                            "average_amount": {
                              "avg": {
                                "field": "amount"
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """.formatted(gte, lte, sessionId);
    }
}
