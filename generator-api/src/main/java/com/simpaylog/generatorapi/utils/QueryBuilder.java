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
}
