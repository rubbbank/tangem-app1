package com.tangem.domain.redux.state

import com.tangem.domain.redux.DomainState
import org.rekotlin.Action

/**
 * Created by Anton Zhilenkov on 07/04/2022.
 */
interface StatePrinter<A, S> {
    fun print(action: Action, domainState: DomainState): String?
    fun getStateObject(domainState: DomainState): S
}