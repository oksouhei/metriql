package com.metriql.db.mssqlserver

import com.metriql.tests.SimpleFilterTests
import com.metriql.tests.TestSimpleFilter
import com.metriql.warehouse.mssqlserver.MSSQLDataSource
import org.testng.annotations.BeforeSuite
import java.time.ZoneOffset

class TestSimpleFilterMSSQLServer : TestSimpleFilter() {
    override val testingServer = TestingEnvironmentMSSQLServer
    override val dataSource = MSSQLDataSource(testingServer.config)

    @BeforeSuite
    fun setup() {
        testingServer.init()
        populate()
    }

    override fun populate() {
        testingServer.createConnection().use { connection ->
            // Create table
            connection.createStatement().execute(
                """
                CREATE TABLE ${testingServer.getTableReference(table)} (
                    test_int INTEGER,
                    test_string VARCHAR(255),
                    test_double FLOAT,
                    test_date DATE,
                    test_bool INTEGER,
                    test_timestamp DATETIME
                )
                """.trimIndent()
            )

            // Populate data
            // FROM_UNIXTIME accepts seconds
            val values = SimpleFilterTests.testInt.mapIndexed { index, i ->
                """(
                    $i,
                    '${SimpleFilterTests.testString[index]}',
                    ${SimpleFilterTests.testDouble[index]},
                    CAST('${SimpleFilterTests.testDate[index]}' AS DATE),
                    ${if (SimpleFilterTests.testBool[index]) 1 else 0},
                    DATEADD(s, ${SimpleFilterTests.testTimestamp[index].toEpochSecond(ZoneOffset.UTC)}, '19700101')
                    )
                """.trimIndent()
            }
            connection.createStatement().execute(
                """
                INSERT INTO ${testingServer.getTableReference(table)} (
                test_int,
                test_string,
                test_double,
                test_date,
                test_bool,
                test_timestamp)
                VALUES ${values.joinToString(", ")}
                """.trimIndent()
            )
        }
    }
}
