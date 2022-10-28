package com.tangem.tap.common.analytics.handlers.appsFlyer

import com.tangem.tap.common.analytics.AnalyticsEventsLogger

/**
 * Created by Anton Zhilenkov on 22/09/2022.
 */
internal class AppsFlyerLogClient(
    private val logger: AnalyticsEventsLogger,
) : AppsFlyerAnalyticsClient {

    override fun logEvent(event: String, params: Map<String, String>) {
        logger.logEvent(event, params)
    }
}
