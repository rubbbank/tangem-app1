package com.tangem.tangemtest.card_use_cases.ui

import com.tangem.tangemtest.R
import com.tangem.tangemtest.commons.Action

/**
 * Created by Anton Zhilenkov on 10.03.2020.
 */
class ScanActionFragment : BaseCardActionFragment() {

    override fun getLayoutId(): Int = R.layout.fg_action_card_scan

    override fun getAction(): Action = Action.Scan
}