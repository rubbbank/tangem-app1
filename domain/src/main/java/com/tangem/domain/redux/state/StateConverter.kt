package com.tangem.domain.redux.state

import org.rekotlin.Action

/**
 * Created by Anton Zhilenkov on 07/04/2022.
 */
interface StringStateConverter<StateHolder> {
    fun convert(stateHolder: StateHolder): String
}

interface StringActionConverter<StateHolder> {
    fun convert(action: Action, stateHolder: StateHolder): String?
}