package io.trino

import com.metriql.service.model.IModelService
import io.trino.connector.system.SystemHandleResolver
import io.trino.connector.system.SystemPageSourceProvider
import io.trino.connector.system.SystemSplitManager
import io.trino.connector.system.SystemTablesMetadata
import io.trino.connector.system.SystemTransactionHandle
import io.trino.metadata.InternalNodeManager
import io.trino.spi.connector.Connector
import io.trino.spi.connector.ConnectorContext
import io.trino.spi.connector.ConnectorFactory
import io.trino.spi.connector.ConnectorHandleResolver
import io.trino.spi.connector.ConnectorPageSourceProvider
import io.trino.spi.connector.ConnectorSplitManager
import io.trino.spi.connector.ConnectorTransactionHandle
import io.trino.spi.transaction.IsolationLevel
import io.trino.transaction.InternalConnector
import io.trino.transaction.TransactionId

class MetriqlConnectorFactory(private val internalNodeManager: InternalNodeManager, val modelService: IModelService) : ConnectorFactory {
    override fun getName() = "metriql"

    override fun create(catalogName: String?, config: MutableMap<String, String>?, context: ConnectorContext?): Connector {
        return MetriqlConnector(internalNodeManager, modelService)
    }

    override fun getHandleResolver(): ConnectorHandleResolver {
        return SystemHandleResolver()
    }

    class MetriqlConnector(private val nodeManager: InternalNodeManager, val modelService: IModelService) : InternalConnector {
        val metadata = MetriqlMetadata(modelService)

        override fun beginTransaction(transactionId: TransactionId, isolationLevel: IsolationLevel?, readOnly: Boolean): ConnectorTransactionHandle {
            return MetriqlTransactionHandle(transactionId)
        }

        override fun getSplitManager(): ConnectorSplitManager {
            return SystemSplitManager(nodeManager, metadata)
        }

        override fun beginTransaction(isolationLevel: IsolationLevel?, readOnly: Boolean): ConnectorTransactionHandle {
            return super.beginTransaction(isolationLevel, readOnly)
        }

        override fun getPageSourceProvider(): ConnectorPageSourceProvider {
            return SystemPageSourceProvider(metadata)
        }

        override fun getMetadata(transactionHandle: ConnectorTransactionHandle?) = SystemTablesMetadata(metadata)
    }

    class MetriqlTransactionHandle(transactionId: TransactionId?) : SystemTransactionHandle(transactionId, object : ConnectorTransactionHandle {})
}
