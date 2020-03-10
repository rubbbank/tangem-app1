package com.tangem.tangemtest.commons

import android.content.Context
import androidx.fragment.app.Fragment
import com.tangem.tangemtest.card_use_cases.CardContext

/**
 * Created by Anton Zhilenkov on 10.03.2020.
 */

interface DiManager {
    fun getCardContext(): CardContext
}

fun Context.getDiManager(): DiManager = applicationContext as DiManager

fun Fragment.getDiManager(): DiManager = activity?.applicationContext as DiManager