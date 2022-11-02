package com.tangem.tap.common.analytics.api

import com.tangem.tap.common.analytics.events.AnalyticsEvent

/**
 * Created by Anton Zhilenkov on 02.11.2022.
 */
interface AnalyticsEventFilter {
    fun canBeAppliedTo(event: AnalyticsEvent): Boolean
    fun canBeSent(event: AnalyticsEvent): Boolean
    fun canBeConsumedBy(handler: AnalyticsEventHandler, event: AnalyticsEvent): Boolean
}
