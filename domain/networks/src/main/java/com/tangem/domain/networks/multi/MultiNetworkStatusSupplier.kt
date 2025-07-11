package com.tangem.domain.networks.multi

import com.tangem.domain.core.flow.FlowCachingSupplier
import com.tangem.domain.tokens.model.NetworkStatus

/**
 * Supplier of all networks statuses for selected wallet [MultiNetworkStatusProducer.Params]
 *
 * @property factory    factory for creating [MultiNetworkStatusProducer]
 * @property keyCreator key creator
 *
 * @author Andrew Khokhlov on 05/03/2025
 */
abstract class MultiNetworkStatusSupplier(
    override val factory: MultiNetworkStatusProducer.Factory,
    override val keyCreator: (MultiNetworkStatusProducer.Params) -> String,
) : FlowCachingSupplier<MultiNetworkStatusProducer, MultiNetworkStatusProducer.Params, Set<NetworkStatus>>()
