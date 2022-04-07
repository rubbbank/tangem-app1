package com.tangem.domain.features.global.redux

import com.tangem.domain.common.ScanResponse

/**
 * Created by Anton Zhilenkov on 07/04/2022.
 */
//TODO: refactoring: is alias for the GlobalState
data class DomainGlobalState(
    val scanResponse: ScanResponse? = null,
)

