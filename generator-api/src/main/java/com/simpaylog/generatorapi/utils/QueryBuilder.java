package com.simpaylog.generatorapi.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static String timeHeatmapQuery(String sessionId, LocalDate from, LocalDate to) {
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
                          { "term": { "sessionId": "%s" }  },
                          { "term": { "transactionType": "WITHDRAW" } },
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
                      "by_day": {
                        "terms": {
                          "script": {
                                  "lang": "painless",
                                  "source": "def dow = doc['timestamp'].value.getDayOfWeekEnum().getValue(); dow = dow + 1 > 7 ? dow + 1 - 7 : dow + 1; return dow;"
                                },
                          "size": 7,
                          "order": { "_key": "asc" }
                        },
                        "aggs": {
                          "by_hour": {
                            "terms": {
                              "script": {
                                            "lang": "painless",
                                            "source": "def hour = doc['timestamp'].value.getHour() + 9; return hour >= 24 ? hour - 24 : hour;"
                                          },
                              "size": 24,
                              "order": { "_key": "asc" }
                            }
                          }
                        }
                      }
                    }
                  }
                """.formatted(sessionId, gte, lte);
    }

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
                        "size": 10
                      },
                      "aggs": {
                        "by_hour": {
                          "terms": {
                            "script": {
                              "lang": "painless",
                              "source": "ZonedDateTime kst = ZonedDateTime.ofInstant(doc['timestamp'].value.toInstant(), ZoneId.of('Asia/Seoul')); return kst.getHour();"
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
                                "script": {
                                  "source": "Math.round(_value)",
                                  "lang": "painless"
                                },
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

    public static String incomeExpenseByAgeGroupQuery(String sessionId, List<Long> userIds) {
        return """
                {
                  "size": 0,
                  "query": {
                    "bool": {
                      "must": [
                        {
                          "term": {
                            "sessionId": "%s"
                          }
                        },
                        {
                          "terms": {
                            "userId": %s
                          }
                        }
                      ]
                    }
                  },
                  "aggs": {
                    "financial_summary": {
                      "filters": {
                        "filters": {
                          "income": {
                            "term": {
                              "transactionType": "DEPOSIT"
                            }
                          },
                          "expense": {
                            "bool": {
                              "must_not": {
                                "term": {
                                  "transactionType": "DEPOSIT"
                                }
                              }
                            }
                          }
                        }
                      },
                      "aggs": {
                        "total_amount": {
                          "sum": {
                            "script": {
                              "source": "doc.containsKey('amount') ? doc['amount'].value : 0"
                            }
                          }
                        }
                      }
                    }
                  }
                }
        """.formatted(sessionId, userIds.toString());
    }

    public static String test(
            String sessionId,
            Map<Integer, List<Long>> ageGroupUserIds,
            String durationStart,
            String durationEnd)
    {
        String filtersJson = ageGroupUserIds.entrySet().stream()
                // ID 목록이 비어있는 나이대는 쿼리에서 제외
                .filter(entry -> !entry.getValue().isEmpty())
                // 각 항목을 "10s": { "terms": { "userId": [101, 105] }} 형태의 문자열로 변환
                .map(entry -> {
                    String ageGroupKey = "\"" + entry.getKey() + "대" + "\"";
                    String userIdsArray = entry.getValue().toString();
                    return String.format("%s: { \"terms\": { \"userId\": %s }}", ageGroupKey, userIdsArray);
                })
                // 변환된 모든 문자열을 콤마(,)로 연결
                .collect(Collectors.joining(", "));
        return String.format(
"""
{
  "size": 0,
  "query": {
    "bool": {
      "must": [
        {
          "range": {
            "timestamp": {
              "from": "%s",
              "to": "%s",
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
    "age_group_summary": {
      "filters": { "filters": { %s }},
      "aggs": { "income_expense_split": { "filters": { "filters": {
        "income": { "term": { "transactionType": "DEPOSIT" }},
        "expense": { "bool": { "must_not": { "term": { "transactionType": "DEPOSIT" }}}}
      }},
      "aggs": { "total_amount": { "sum": { "script": { "source": "doc.containsKey('amount') ? doc['amount'].value : 0" }}},
               "user_count": { "cardinality": { "field": "userId" }},
               "average_per_user": { "bucket_script": {
                 "buckets_path": { "total": "total_amount", "count": "user_count" },
                 "script": "params.count > 0 ? params.total / params.count : 0"
               }}
      }}}
    }
  }
}
""", durationStart, durationEnd, sessionId, filtersJson);
    }
}
