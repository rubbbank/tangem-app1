package com.tangem.tangemtest.ucase.domain.paramsManager

import com.tangem.tangemtest.ucase.domain.paramsManager.managers.DepersonalizeParamsManager
import com.tangem.tangemtest.ucase.domain.paramsManager.managers.PersonalizeParamsManager
import com.tangem.tangemtest.ucase.domain.paramsManager.managers.ScanParamsManager
import com.tangem.tangemtest.ucase.domain.paramsManager.managers.SignParamsManager
import com.tangem.tangemtest.ucase.resources.ActionType
import ru.dev.gbixahue.eu4d.lib.kotlin.common.BaseTypedHolder

/**
 * Created by Anton Zhilenkov on 12.03.2020.
 */
class ParamsManagerFactory : BaseTypedHolder<ActionType, ParamsManager>() {

    companion object {
        fun createFactory(): ParamsManagerFactory {
            return ParamsManagerFactory().apply {
                register(ActionType.Scan, ScanParamsManager())
                register(ActionType.Sign, SignParamsManager())
                register(ActionType.Personalize, PersonalizeParamsManager())
                register(ActionType.Depersonalize, DepersonalizeParamsManager())
            }
        }
    }
}