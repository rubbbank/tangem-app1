package com.tangem.tangemtest

import android.app.Application
import com.tangem.tangemtest.card_use_cases.CardContext
import com.tangem.tangemtest.commons.DiManager

/**
 * Created by Anton Zhilenkov on 10.03.2020.
 */
class AppTangemDemo : Application(), DiManager {
    private val cardContext: CardContext = CardContext()

    override fun getCardContext(): CardContext = cardContext
}