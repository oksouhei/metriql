package com.metriql.deployment

import com.metriql.UserContext
import com.metriql.service.auth.ProjectAuth
import com.metriql.service.model.IModelService
import com.metriql.warehouse.spi.DataSource

interface Deployment {
    fun getModelService(): IModelService
    fun logStart()
    fun getDataSource(auth: ProjectAuth): DataSource
    fun getAuth(context: UserContext): ProjectAuth
    fun isAnonymous() : Boolean
}
