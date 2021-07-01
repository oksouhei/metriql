package io.trino

import com.google.common.collect.ImmutableList
import com.metriql.db.FieldType
import com.metriql.service.jdbc.extractModelNameFromPropertiesTable
import com.metriql.service.model.Model
import io.trino.connector.system.SystemTablesProvider
import io.trino.spi.connector.ColumnMetadata
import io.trino.spi.connector.ConnectorSession
import io.trino.spi.connector.ConnectorTableMetadata
import io.trino.spi.connector.ConnectorTransactionHandle
import io.trino.spi.connector.InMemoryRecordSet
import io.trino.spi.connector.RecordCursor
import io.trino.spi.connector.SchemaTableName
import io.trino.spi.connector.SystemTable
import io.trino.spi.predicate.TupleDomain
import io.trino.spi.type.BigintType
import io.trino.spi.type.BooleanType
import io.trino.spi.type.DateType
import io.trino.spi.type.DecimalType
import io.trino.spi.type.DoubleType
import io.trino.spi.type.IntegerType
import io.trino.spi.type.TimeType
import io.trino.spi.type.TimestampType
import io.trino.spi.type.TimestampWithTimeZoneType
import io.trino.spi.type.Type
import io.trino.spi.type.VarcharType
import io.trino.type.UnknownType
import java.util.Optional

class MetriqlMetadata(val models: List<Model>) : SystemTablesProvider {

    override fun listSystemTables(session: ConnectorSession?): Set<SystemTable> {
        return models.map { ModelProxy(models, it) }.toSet()
    }

    override fun getSystemTable(session: ConnectorSession?, tableName: SchemaTableName): Optional<SystemTable> {
        val modelCategory = getCategoryFromSchema(tableName.schemaName)
        val propertiesForModel = extractModelNameFromPropertiesTable(tableName.tableName)
        val name = propertiesForModel ?: tableName.tableName
        val model = models.find { it.category?.lowercase() == modelCategory && it.name == name } ?: return Optional.empty()

        return if (propertiesForModel != null) {
            Optional.of(TablePropertiesTable(model))
        } else {
            Optional.of(ModelProxy(models, model))
        }
    }

    private fun getCategoryFromSchema(schemaName: String): Any? {
        return if (schemaName == "public") null else schemaName
    }

    class TablePropertiesTable(val model: Model) : SystemTable {
        private val metadata = ConnectorTableMetadata(
            SchemaTableName(model.category ?: "public", model.name),
            ImmutableList.of(
                ColumnMetadata("comment", VarcharType.VARCHAR)
            )
        )

        override fun getDistribution() = SystemTable.Distribution.SINGLE_COORDINATOR

        override fun getTableMetadata(): ConnectorTableMetadata {
            return metadata
        }

        override fun cursor(transactionHandle: ConnectorTransactionHandle, session: ConnectorSession, constraint: TupleDomain<Int>): RecordCursor {
            val table = InMemoryRecordSet.builder(metadata)
            table.addRow(model.description)
            return table.build().cursor()
        }
    }

    class ModelProxy(val models: List<Model>, val model: Model) : SystemTable {
        override fun getDistribution() = SystemTable.Distribution.SINGLE_COORDINATOR

        private fun toColumnMetadata(it: Model.Measure, relation: String? = null): ColumnMetadata? {
            if (it.value.agg != null) {
                return null
            }

            return ColumnMetadata.builder()
                .setName(relation?.let { _ -> "$relation.${it.name}" } ?: it.name)
                .setComment(Optional.ofNullable(it.description))
                .setHidden(it.hidden ?: false)
                .setNullable(true)
                .setType(getTrinoType(it.fieldType))
                .build()
        }

        private fun toColumnMetadata(it: Model.Dimension, relation: String? = null, postOperation: String? = null): ColumnMetadata {
            val relationPrefix = relation?.let { "$it." } ?: ""
            val postOperationSuffix = postOperation?.let { "::$it" } ?: ""
            return ColumnMetadata.builder()
                .setName(relationPrefix + it.name + postOperationSuffix)
                .setComment(Optional.ofNullable(it.description))
                .setHidden(it.hidden ?: false)
                .setNullable(true)
                .setType(getTrinoType(it.fieldType))
                .build()
        }

        private fun addColumnsForModel(columns: MutableList<ColumnMetadata>, model: Model, relation: Model.Relation?) {
            model.dimensions.forEach { dimension ->
                if (dimension.postOperations != null) {
                    dimension.postOperations.forEach { timeframe -> columns.add(toColumnMetadata(dimension, relation?.name, timeframe)) }
                } else columns.add(toColumnMetadata(dimension, relation?.name))
            }
            model.measures.filter { it.value.agg != null }.mapNotNull { toColumnMetadata(it, relation?.name) }.forEach { columns.add(it) }
        }

        override fun getTableMetadata(): ConnectorTableMetadata {
            val columns = mutableListOf<ColumnMetadata>()

            addColumnsForModel(columns, model, null)

            model.relations?.forEach { relation ->
                models.find { it.name == relation.modelName }?.let { model ->
                    addColumnsForModel(columns, model, relation)
                }
            }

            return ConnectorTableMetadata(
                SchemaTableName(model.category ?: "public", model.name),
                columns,
                mapOf("label" to model.label),
                Optional.ofNullable(model.description)
            )
        }
    }

    companion object {
        private val decimalType = DecimalType.createDecimalType()

        fun getTrinoType(type: FieldType?): Type {
            return when (type) {
                FieldType.INTEGER -> IntegerType.INTEGER
                FieldType.STRING -> VarcharType.createVarcharType(100)
                FieldType.DECIMAL -> decimalType
                FieldType.DOUBLE -> DoubleType.DOUBLE
                FieldType.LONG -> BigintType.BIGINT
                FieldType.BOOLEAN -> BooleanType.BOOLEAN
                FieldType.DATE -> DateType.DATE
                FieldType.TIME -> TimeType.TIME_MILLIS
                FieldType.TIMESTAMP -> TimestampType.TIMESTAMP_MILLIS
                FieldType.BINARY -> TODO()
                FieldType.ARRAY_STRING -> TODO()
                FieldType.ARRAY_INTEGER -> TODO()
                FieldType.ARRAY_DOUBLE -> TODO()
                FieldType.ARRAY_LONG -> TODO()
                FieldType.ARRAY_BOOLEAN -> TODO()
                FieldType.ARRAY_DATE -> TODO()
                FieldType.ARRAY_TIME -> TODO()
                FieldType.ARRAY_TIMESTAMP -> TODO()
                FieldType.MAP_STRING -> TODO()
                FieldType.UNKNOWN, null -> UnknownType.UNKNOWN
            }
        }

        fun getMetriqlType(type: Type): FieldType {
            val name = type.baseName

            return when {
                IntegerType.INTEGER.baseName == name -> FieldType.INTEGER
                VarcharType.VARCHAR.baseName == name -> FieldType.STRING
                decimalType.baseName == name -> FieldType.DECIMAL
                DoubleType.DOUBLE.baseName == name -> FieldType.DOUBLE
                BigintType.BIGINT.baseName == name -> FieldType.LONG
                BooleanType.BOOLEAN.baseName == name -> FieldType.BOOLEAN
                DateType.DATE.baseName == name -> FieldType.DATE
                TimeType.TIME_MILLIS.baseName == name -> FieldType.TIME
                TimestampType.TIMESTAMP_MILLIS.baseName == name -> FieldType.TIMESTAMP
                TimestampWithTimeZoneType.TIMESTAMP_TZ_MILLIS.baseName == name -> FieldType.TIMESTAMP
                type.baseName == "array" -> TODO()
                type.baseName == "map" -> TODO()
                else -> throw UnsupportedOperationException("Unable to identify $type")
            }
        }
    }
}
