package com.tangem.tangemtest.ucase.domain.actions

import com.tangem.tangemtest.ucase.domain.paramsManager.ActionCallback
import com.tangem.tangemtest.ucase.domain.paramsManager.triggers.afterAction.AfterScanModifier

/**
 * Created by Anton Zhilenkov on 19/03/2020.
 */
class ScanAction : BaseCardAction() {
    override fun executeMainAction(attrs: AttrForAction, callback: ActionCallback) {
        attrs.cardManager.scanCard { handleResponse(it, AfterScanModifier(), attrs, callback) }
    }
}