package com.tangem.tangemtest.card_use_cases.ui

import com.tangem.tangemtest.R
import com.tangem.tangemtest.commons.ActionType

/**
 * Created by Anton Zhilenkov on 10.03.2020.
 */
class SignActionFragment : BaseCardActionFragment() {

    override fun getLayoutId(): Int = R.layout.fg_action_card_sign

    override fun getAction(): ActionType = ActionType.Sign
}