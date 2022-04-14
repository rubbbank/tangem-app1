package com.tangem.domain

/**
 * Created by Anton Zhilenkov on 13/04/2022.
 * Must be handled by the module or sent to Crashlytics
 */
interface DomainInternalException

sealed class DomainException(message: String?) : Throwable(message), DomainInternalException {
    data class SelectTokeNetworkException(val networkId: String) : DomainException(
        "Unknown network [$networkId] should not be included in the network selection dialog."
    )

    data class UnAppropriateInitializationException(val of: String, val info: String? = null) : DomainException(
        "The [$of], must be properly initialized. Info []"
    )
}