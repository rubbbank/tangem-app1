package com.tangem.domain.common.extensions

import com.tangem.common.card.FirmwareVersion

/**
 * Created by Anton Zhilenkov on 28/04/2022.
 */
val FirmwareVersion.Companion.SolanaAvailable
    get() = FirmwareVersion(4, 12)

val FirmwareVersion.Companion.SolanaTokensAvailable
    get() = FirmwareVersion(4, 52)