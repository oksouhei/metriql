package com.metriql.warehouse.spi

import com.metriql.db.QueryResult
import com.metriql.report.QueryTask
import com.metriql.service.jinja.SQLRenderable
import com.metriql.service.model.Model
import com.metriql.service.task.Task

interface DataSource {
    open val warehouse: Warehouse<*>
    open val config: Warehouse.Config

    // Database Related
    fun listDatabaseNames(): List<DatabaseName>

    // Schema Related
    fun listSchema(database: String?, schema: String?, tables: Collection<String>?): Collection<TableSchema>

    fun listSchemaNames(database: String?): List<SchemaName>

    fun preview(auth: WarehouseAuth, target: Model.Target): Task<*, *> = throw UnsupportedOperationException()

    // Table Related
    fun listTableNames(database: String?, schema: String?): List<TableName>
    fun getTable(database: String?, schema: String?, table: String): TableSchema
    fun getTable(sql: String): TableSchema

    fun connectionTest(userId: Int): Boolean

    fun sqlReferenceForTarget(
        target: Model.Target,
        aliasName: String,
        columnName: String
    ): String

    fun sqlReferenceForTarget(
        target: Model.Target,
        aliasName: String,
        renderable: (SQLRenderable) -> String
    ): String

    fun fillDefaultsToTarget(target: Model.Target): Model.Target

    fun dbtSettings(): DbtSettings {
        throw IllegalStateException("dbt is not supported for this database")
    }

    // Query execution
    fun createQueryTask(
        auth: WarehouseAuth,
        query: QueryResult.QueryStats.QueryInfo,
        defaultSchema: String?,
        defaultDatabase: String?,
        limit: Int?,
        isBackgroundTask: Boolean
    ): QueryTask
}
