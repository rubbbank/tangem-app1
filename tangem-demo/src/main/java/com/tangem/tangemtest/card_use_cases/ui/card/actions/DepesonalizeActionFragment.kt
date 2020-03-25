package com.tangem.tangemtest.card_use_cases.ui.card.actions

import com.tangem.tangemtest.R
import com.tangem.tangemtest.card_use_cases.resources.ActionType
import com.tangem.tangemtest.card_use_cases.ui.card.BaseCardActionFragment

/**
 * Created by Anton Zhilenkov on 10.03.2020.
 */
class DepesonalizeActionFragment : BaseCardActionFragment() {

    override fun getLayoutId(): Int = R.layout.fg_depersonalize

    override fun getAction(): ActionType = ActionType.Depersonalize

}