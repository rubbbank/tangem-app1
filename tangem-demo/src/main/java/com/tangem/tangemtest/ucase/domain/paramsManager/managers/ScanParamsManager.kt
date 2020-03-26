package com.tangem.tangemtest.ucase.domain.paramsManager.managers

import com.tangem.CardManager
import com.tangem.common.tlv.TlvTag
import com.tangem.tangemtest.ucase.domain.actions.ScanAction
import com.tangem.tangemtest.ucase.domain.paramsManager.ActionCallback
import com.tangem.tangemtest.ucase.domain.paramsManager.IncomingParameter

/**
 * Created by Anton Zhilenkov on 19/03/2020.
 */
class ScanParamsManager : BaseParamsManager(ScanAction()) {
    override fun createParamsList(): List<IncomingParameter> {
        return listOf(IncomingParameter(TlvTag.CardId, null))
    }

    override fun invokeMainAction(cardManager: CardManager, callback: ActionCallback) {
        action.executeMainAction(getAttrsForAction(cardManager), callback)
    }
}