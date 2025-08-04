package com.simpaylog.generatorapi.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QueryBuilder {

    public static String periodAggregationQuery(String sessionId, LocalDate from, LocalDate to, String interval) {
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
                        { "term": {"sessionId": "%s"} },
                        {
                          "range": {
                            "timestamp": {
                              "gte": "%s",
                              "lte": "%s",
                              "time_zone": "Asia/Seoul"
                            }
                          }
                        }
                      ]
                    }
                  },
                  "aggs": {
                    "results": {
                      "date_histogram": {
                        "field": "timestamp",
                        "calendar_interval": "%s",
                        "time_zone": "Asia/Seoul",
                        "format": "yyyy-MM-dd"
                      },
                      "aggs": {
                        "total_spent": {
                          "filter": {
                            "term": {
                              "transactionType": "WITHDRAW"
                            }
                          },
                          "aggs": {
                            "amount_sum": {
                              "sum": {
                                "field": "amount"
                              }
                            }
                          }
                        },
                        "total_income": {
                          "filter": {
                            "term": {
                              "transactionType": "DEPOSIT"
                            }
                          },
                          "aggs": {
                            "amount_sum": {
                              "sum": {
                                "field": "amount"
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """.formatted(sessionId, gte, lte, interval);
    }
}
