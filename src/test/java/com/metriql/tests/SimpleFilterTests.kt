package com.metriql.tests

import com.metriql.db.FieldType
import com.metriql.report.data.ReportFilter
import com.metriql.report.data.ReportFilter.FilterValue.MetricFilter
import com.metriql.report.data.ReportMetric.ReportDimension
import com.metriql.service.model.ModelName
import com.metriql.warehouse.spi.filter.AnyOperatorType
import com.metriql.warehouse.spi.filter.BooleanOperatorType
import com.metriql.warehouse.spi.filter.DateOperatorType
import com.metriql.warehouse.spi.filter.NumberOperatorType
import com.metriql.warehouse.spi.filter.StringOperatorType
import com.metriql.warehouse.spi.filter.TimestampOperatorType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object SimpleFilterTests {

    // 1000 * 60 * 60 = 1 Hour
    val testInt = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    val testString = listOf("alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel", "india", "juliett")
    val testBool = listOf(false, true, true, true, true, true, true, true, true, true)
    val testDouble = testInt.map { it * 1.0 }
    val testDate = testInt.map { LocalDate.of(2000, 1, it + 1) }
    val testTimestamp = testInt.map { LocalDateTime.ofEpochSecond((it * 60 * 60).toLong(), 0, ZoneOffset.UTC) }

    interface OperatorTests {
        fun filter(modelName: ModelName): List<ReportFilter>
        val result: Any?
    }

    enum class ComplexTest : OperatorTests {
        COMPLEX_1 {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_int", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.INTEGER, NumberOperatorType.EQUALS, 1),
                                MetricFilter.Filter(null, null, FieldType.INTEGER, NumberOperatorType.EQUALS, 2),
                                MetricFilter.Filter(null, null, FieldType.INTEGER, NumberOperatorType.EQUALS, 3)
                            )
                        )
                    ),
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_string", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.STRING, StringOperatorType.EQUALS, "charlie")
                            )
                        )
                    )
                )
            }

            override val result: List<String> = listOf("charlie")
        },
        COMPLEX_2 {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_int", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.INTEGER, NumberOperatorType.EQUALS, 1),
                                MetricFilter.Filter(null, null, FieldType.INTEGER, NumberOperatorType.EQUALS, 2),
                                MetricFilter.Filter(null, null, FieldType.INTEGER, NumberOperatorType.LESS_THAN, 6)
                            )
                        )
                    ),
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_string", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.STRING, StringOperatorType.EQUALS, "charlie")
                            )
                        )
                    )
                )
            }

            override val result: List<String> = listOf("charlie")
        }
    }

    enum class AnyOperatorTest : OperatorTests {
        IS_SET {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_int", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.STRING, AnyOperatorType.IS_SET, null)
                            )
                        )
                    )
                )
            }

            override val result: List<String> = listOf(testString[0])
        },

        IS_NOT_SET {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_int", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.STRING, AnyOperatorType.IS_NOT_SET, null)
                            )
                        )
                    )
                )
            }

            override val result = null
        };
    }

    enum class StringOperatorTest : OperatorTests {
        EQUALS {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_string", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.STRING, StringOperatorType.EQUALS, "alpha")
                            )
                        )
                    )
                )
            }

            override val result: List<String> = listOf("alpha")
        },

        NOT_EQUALS {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_string", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.STRING, StringOperatorType.NOT_EQUALS, "alpha")
                            )
                        ),
                    ),
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_string", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.STRING, StringOperatorType.EQUALS, "bravo")
                            )
                        ),
                    )
                )
            }

            override val result: List<String> = listOf("bravo")
        },

        CONTAINS {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_string", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.STRING, StringOperatorType.CONTAINS, "liet")
                            )
                        )
                    )
                )
            }

            override val result: List<String> = listOf("juliett")
        },

        STARTS_WITH {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_string", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.STRING, StringOperatorType.STARTS_WITH, "charli")
                            )
                        )
                    )
                )
            }

            override val result: List<String> = listOf("charlie")
        },

        ENDS_WITH {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_string", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.STRING, StringOperatorType.ENDS_WITH, "trot")
                            )
                        )
                    )
                )
            }

            override val result: List<String> = listOf("foxtrot")
        },

        IN {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_string", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.STRING, StringOperatorType.IN, listOf("alpha"))
                            )
                        )
                    )
                )
            }

            override val result: List<String> = listOf("alpha")
        },

        NOT_IN {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_string", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.STRING, StringOperatorType.NOT_IN, listOf("alpha"))
                            )
                        )
                    )
                )
            }

            override val result: List<String> = listOf("alpha")
        },

        EQUALS_MULTI {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_string", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.STRING, StringOperatorType.IN, listOf("alpha"))
                            )
                        )
                    )
                )
            }

            override val result: List<String> = listOf("alpha")
        },
    }

    enum class BooleanTest : OperatorTests {
        IS {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_bool", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.BOOLEAN, BooleanOperatorType.IS, true)
                            )
                        )
                    )
                )
            }

            override val result = listOf(9.0)
        }
    }

    enum class NumberTest : OperatorTests {
        EQUALS_INT {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_double", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.INTEGER, NumberOperatorType.EQUALS, 1)
                            )
                        )
                    )
                )
            }

            override val result = listOf(1.0)
        },

        EQUALS_DOUBLE {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_double", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.DOUBLE, NumberOperatorType.EQUALS, 1.0)
                            )
                        )
                    )
                )
            }

            override val result = listOf(1.0)
        },

        GREATER_THAN_INT {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_int", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.INTEGER, NumberOperatorType.GREATER_THAN, 0)
                            )
                        )
                    )
                )
            }

            override val result = listOf(1.0)
        },

        GREATER_THAN_DOUBLE {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_double", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.DOUBLE, NumberOperatorType.GREATER_THAN, 0)
                            )
                        )
                    )
                )
            }

            override val result = listOf(1.0)
        },

        LESS_THAN_INT {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_int", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.INTEGER, NumberOperatorType.LESS_THAN, 1)
                            )
                        )
                    )
                )
            }

            override val result = listOf(0.0)
        },

        LESS_THAN_DOUBLE {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_double", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.DOUBLE, NumberOperatorType.LESS_THAN, 1.0)
                            )
                        )
                    )
                )
            }

            override val result = listOf(0.0)
        },

        GREATER_THAN_AND_LESS_THAN_INT {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_int", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.INTEGER, NumberOperatorType.GREATER_THAN, 3)
                            )
                        )
                    ),
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_int", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.INTEGER, NumberOperatorType.LESS_THAN, 5)
                            )
                        )
                    )
                )
            }

            override val result = listOf(4.0)
        },

        GREATER_THAN_AND_LESS_THAN_DOUBLE {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_double", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.DOUBLE, NumberOperatorType.GREATER_THAN, 3.0)
                            )
                        )
                    ),
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_double", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.DOUBLE, NumberOperatorType.LESS_THAN, 5.0)
                            )
                        )
                    )
                )
            }

            override val result = listOf(4.0)
        };
    }

    enum class TimestampOperatorTest : OperatorTests {
        GREATER_THAN {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_timestamp", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(
                                    null, null,
                                    FieldType.TIMESTAMP,
                                    TimestampOperatorType.GREATER_THAN,
                                    testTimestamp.last().format(DateTimeFormatter.ISO_DATE_TIME)
                                )
                            )
                        )
                    )
                )
            }

            override val result = null
        },

        LESS_THAN {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_timestamp", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(
                                    null, null,
                                    FieldType.TIMESTAMP,
                                    TimestampOperatorType.LESS_THAN,
                                    testTimestamp[1].toString()
                                )
                            )
                        )
                    )
                )
            }

            override val result: List<LocalDateTime> = listOf(testTimestamp.first())
        },

        GREATER_THAN_AND_LESS_THAN {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_timestamp", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(
                                    null, null,
                                    FieldType.TIMESTAMP,
                                    TimestampOperatorType.GREATER_THAN,
                                    testTimestamp[3].toString()
                                )
                            )
                        )
                    ),
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_timestamp", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(
                                    null, null,
                                    FieldType.TIMESTAMP,
                                    TimestampOperatorType.LESS_THAN,
                                    testTimestamp[5].toString()
                                )
                            )
                        )
                    )
                )
            }

            override val result = listOf(testTimestamp[4])
        },
    }

    enum class DateOperatorTests : OperatorTests {
        GREATER_THAN {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_date", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.DATE, DateOperatorType.GREATER_THAN, testDate.last().toString())
                            )
                        )
                    )
                )
            }

            override val result = null
        },

        LESS_THAN {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_date", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.DATE, DateOperatorType.LESS_THAN, testDate[1].toString())
                            )
                        )
                    )
                )
            }

            override val result = listOf(testDate.first())
        },

        GREATER_THAN_AND_LESS_THAN {
            override fun filter(modelName: ModelName): List<ReportFilter> {
                return listOf(
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_date", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(
                                    null, null,
                                    FieldType.DATE,
                                    DateOperatorType.GREATER_THAN,
                                    testDate[3].toString()
                                )
                            )
                        )
                    ),
                    ReportFilter(
                        ReportFilter.Type.METRIC_FILTER,
                        MetricFilter(
                            MetricFilter.MetricType.DIMENSION,
                            ReportDimension("test_date", modelName, null, null),
                            listOf(
                                MetricFilter.Filter(null, null, FieldType.DATE, DateOperatorType.LESS_THAN, testDate[5].toString())
                            )
                        )
                    )
                )
            }

            override val result = listOf(testDate[4])
        };
    }
}
