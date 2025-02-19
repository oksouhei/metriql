package com.metriql.db.postgresql

import com.metriql.tests.SimpleFilterTests
import com.metriql.tests.TestSimpleFilter
import com.metriql.warehouse.postgresql.PostgresqlDataSource
import org.testng.annotations.BeforeSuite

class TestSimpleFilterPostgresql : TestSimpleFilter() {
    override val testingServer = TestingEnvironmentPostgresql
    override val dataSource = PostgresqlDataSource(testingServer.config)

    @BeforeSuite
    fun setup() {
        testingServer.init()
        populate()
    }

    override fun populate() {
        testingServer.createConnection().use { connection ->

            // Create table
            val stmt = connection.createStatement()
            stmt.execute("SET TIME ZONE 'UTC'")
            stmt.execute(
                """
                CREATE TABLE ${testingServer.getTableReference(table)} (
                    test_int INTEGER,
                    test_string VARCHAR,
                    test_double FLOAT,
                    test_date DATE,
                    test_bool BOOLEAN,
                    test_timestamp TIMESTAMP WITH TIME ZONE
                )
                """.trimIndent()
            )

            // Populate data
            val values = SimpleFilterTests.testInt.mapIndexed { index, i ->
                """(
                    $i,
                    '${SimpleFilterTests.testString[index]}',
                    ${SimpleFilterTests.testDouble[index]},
                    CAST('${SimpleFilterTests.testDate[index]}' AS DATE),
                    ${SimpleFilterTests.testBool[index]},
                    '${SimpleFilterTests.testTimestamp[index]}'
                    )
                """.trimIndent()
            }
            stmt.execute(
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
