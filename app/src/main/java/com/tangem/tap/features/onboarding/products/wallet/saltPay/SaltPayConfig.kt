package com.tangem.tap.features.onboarding.products.wallet.saltPay

import com.tangem.tap.common.zendesk.ZendeskConfig

/**
 * Created by Anton Zhilenkov on 13.10.2022.
 */
data class SaltPayConfig(
    val zendesk: ZendeskConfig,
    val kycProvider: KYCProvider,
) {
    companion object {
        fun stub(): SaltPayConfig {
            return SaltPayConfig(
                zendesk = ZendeskConfig("", "", "", "", ""),
                kycProvider = KYCProvider("", "", "", ""),
            )
        }
    }
}

data class KYCProvider(
    val baseUrl: String,
    val externalIdParameterKey: String,
    val sidParameterKey: String,
    val sidValue: String,
)
