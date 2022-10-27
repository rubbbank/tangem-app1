package com.tangem.tap.common.analytics

import com.tangem.tap.common.analytics.api.AnalyticsEventHandler
import com.tangem.tap.common.analytics.api.AnalyticsHandlerBuilder

/**
 * Created by Anton Zhilenkov on 22/09/2022.
 */
class AnalyticsHandlersFactory {

    private val builders = mutableListOf<AnalyticsHandlerBuilder>()

    fun addBuilder(builder: AnalyticsHandlerBuilder) {
        builders.add(builder)
    }

    fun build(data: AnalyticsHandlerBuilder.Data): List<AnalyticsEventHandler> {
        val handlers = builders.mapNotNull { it.build(data) }
        builders.clear()

        return handlers
    }
}
