package com.tangem.tap.common.analytics.handlers.amplitude

import com.tangem.tap.common.analytics.AnalyticsEventsLogger

/**
 * Created by Anton Zhilenkov on 22/09/2022.
 */
internal class AmplitudeLogClient(
    private val logger: AnalyticsEventsLogger,
) : AmplitudeAnalyticsClient {

    override fun logEvent(event: String, params: Map<String, String>) {
        logger.logEvent(event, params)
    }
}
