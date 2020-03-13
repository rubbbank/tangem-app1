package com.tangem.tangemtest.card_use_cases.models.params.manager

import com.tangem.tangemtest.commons.Action
import ru.dev.gbixahue.eu4d.lib.kotlin.common.BaseTypedHolder

/**
 * Created by Anton Zhilenkov on 12.03.2020.
 */
class ParamsManagerFactory : BaseTypedHolder<Action, ParamsManager>() {

    companion object {
        fun createFactory(): ParamsManagerFactory {
            return ParamsManagerFactory().apply {
                register(Action.Scan, ScanParamsManager())
                register(Action.Sign, SignParamsManager())
            }
        }
    }
}