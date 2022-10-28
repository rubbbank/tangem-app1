package com.tangem.tap.common.analytics.events

/**
 * Created by Anton Zhilenkov on 28.09.2022.
 */
sealed class WalletConnect(
    event: String,
    params: Map<String, String> = mapOf(),
) : AnalyticsEvent("Wallet Connect", event, params) {

    class NewSessionEstablished : WalletConnect("New Session Established")
    class SessionDisconnected : WalletConnect("Session Disconnected")
    class RequestSigned : WalletConnect("Request Signed")
}
