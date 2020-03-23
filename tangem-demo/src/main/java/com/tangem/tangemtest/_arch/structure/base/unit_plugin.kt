package com.tangem.tangemtest._arch.structure.base

/**
 * Created by Anton Zhilenkov on 22/03/2020.
 */
typealias PluginCallBackResult = (Any?) -> Unit

interface Plugin : Unit {
    fun invoke(data: Any?, asyncCallback: PluginCallBackResult? = null): Any?
}