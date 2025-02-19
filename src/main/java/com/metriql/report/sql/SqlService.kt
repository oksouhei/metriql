package com.metriql.report.sql

import com.metriql.report.IAdHocService
import com.metriql.report.data.ReportFilter
import com.metriql.service.auth.ProjectAuth
import com.metriql.service.model.ModelName
import com.metriql.warehouse.spi.querycontext.IQueryGeneratorContext

class SqlService : IAdHocService<SqlReportOptions> {

    override fun renderQuery(
        auth: ProjectAuth,
        queryContext: IQueryGeneratorContext,
        reportOptions: SqlReportOptions,
        reportFilters: List<ReportFilter>,
    ): IAdHocService.RenderedQuery {
        val sqlReportOptions = reportOptions as SqlReportOptions

        val compiledSql = queryContext.renderSQL(
            sqlReportOptions.query, null,
            dateRange = ReportFilter.extractDateRangeForEventTimestamp(reportFilters)
        )

        return IAdHocService.RenderedQuery(compiledSql, queryOptions = sqlReportOptions.queryOptions)
    }

    override fun getUsedModels(auth: ProjectAuth, context: IQueryGeneratorContext, reportOptions: SqlReportOptions): Set<ModelName> = setOf()
}
